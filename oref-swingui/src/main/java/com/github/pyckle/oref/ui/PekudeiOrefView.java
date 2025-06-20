package com.github.pyckle.oref.ui;

import com.github.pyckle.oref.alerts.AlertsManager;
import com.github.pyckle.oref.alerts.details.AlertDetails;
import com.github.pyckle.oref.integration.caching.ActiveAlertState;
import com.github.pyckle.oref.integration.caching.AlertStatus;
import com.github.pyckle.oref.integration.caching.OrefApiCachingService;
import com.github.pyckle.oref.integration.config.OrefConfig;
import com.github.pyckle.oref.integration.datetime.OrefDateTimeUtils;
import com.github.pyckle.oref.integration.dto.District;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

public class PekudeiOrefView {
    private static final Logger logger = LoggerFactory.getLogger(PekudeiOrefView.class);

    private static final int NO_TARGET_WIDTH = -1;
    private final OrefConfig orefConfig;

    private final JPanel panel;
    private final List<JLabel> infoLabels = new ArrayList<>();
    private final OrefApiCachingService orefApiCachingService;
    private final AlertsManager alertsManager;
    private AlertStatus alertStatus;
    private boolean activeAlertsDrawn = false;

    private int maxHeight;
    private int maxWidth;

    public PekudeiOrefView(OrefConfig orefConfig) {
        this.orefConfig = orefConfig;
        this.orefApiCachingService = new OrefApiCachingService(orefConfig);
        this.alertsManager = orefApiCachingService.getAlertsManager();
        alertStatus = AlertsManager.emptyStatus();
        panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
    }

    private static int getWidthInPx(String text, Graphics g, Font f) {
        FontMetrics fontMetrics = g.getFontMetrics(f);
        return fontMetrics.stringWidth(text);
    }

    public JPanel getPanel() {
        return panel;
    }

    public void update() {
        try {
            internalUpdateEvents();
        } catch (Exception ex) {
            // don't do anything - rest of clock should stay working.
            logger.error("error updating oref view", ex);
        }
    }

    public void internalUpdateEvents() {
        AlertStatus status = alertsManager.getAlerts();

        // If nothing has changed, return.
        if (alertStatus.getLastUpdate().equals(status.getLastUpdate())) {
            return;
        }

        LocalDateTime activeAlertThreshold = getActiveAlertThreshold();

        // if the alert last retrieved date changed but the alert is identical, update only the last time alerts
        // were fetched, no need to update anything else
        boolean alertsHaveNotChanged = alertStatus.getAlerts().equals(status.getAlerts());
        boolean activeAlertsNotChanged = hasAlert(activeAlertThreshold) == activeAlertsDrawn;
        this.alertStatus = status;
        if (alertsHaveNotChanged && activeAlertsNotChanged) {
            if (!this.infoLabels.isEmpty()) {
                JLabel updateTimeStamp = this.infoLabels.get(0);
                String alertUpdatedStr = getAlertUpdatedStr();
                updateTimeStamp.setText(alertUpdatedStr);
                updateTimeStamp.repaint();
            }
        } else {
            // update everything
            rewriteAlerts(activeAlertThreshold);
        }
    }

    private void rewriteAlerts(LocalDateTime activeAlertThreshold) {
        panel.removeAll();
        activeAlertsDrawn = false;
        infoLabels.clear();

        var alerts = alertStatus.getAlerts();
        GridPlaceTracker tracker;
        if (!alerts.isEmpty()) {
            Font f = getFont(orefConfig.getMinFontSize(), true);
            int maxAlertWidth = maxGroupedAlertTitle(alerts, f);
            tracker = new GridPlaceTracker(this.maxWidth / maxAlertWidth);

            setAlertUpdateStatus(activeAlertThreshold, tracker);
            f = getFont(orefConfig.getMinFontSize(), false);

            District alertArea = alertsManager.alertArea();
            if (alertArea != null)
            {
                LocalDateTime displayThreshold = getRecentAlertTimeframeForAlertArea();
                addNextCellToPanel(tracker, true, Color.WHITE, "Alerts for " + alertArea.label());

                for (AlertDetails alertDetails : alerts)
                {
                    if (tracker.isDone() || alertDetails.remoteTimestamp().isBefore(displayThreshold))
                        break;

                    if (alertDetails.matchesArea(alertArea)) {
                        // match found, need to display.
                        Color alertColor = alertDetails.updateFlashType() != null ?
                                alertDetails.updateFlashType().getColor() :
                                Color.RED;
                        addNextCellToPanel(tracker, true, alertColor, getGroupedAlertTitle(alertDetails));
                    }
                }
            }

            LocalDate currDate = LocalDate.MIN;
            DONE_WITH_ALERTS:
            for (AlertDetails alertDetails : alerts) {
                if (tracker.isDone())
                    break;
                if (!currDate.equals(alertDetails.remoteTimestamp().toLocalDate())) {
                    addNextCellToPanel(tracker, true, Color.WHITE, getDateLabel(alertDetails));
                    if (tracker.isDone()) break;
                    currDate = alertDetails.remoteTimestamp().toLocalDate();
                }
                addNextCellToPanel(tracker, true, Color.PINK, getGroupedAlertTitle(alertDetails));
                if (tracker.isDone())
                    break;

                boolean isActive = alertDetails.remoteTimestamp().isAfter(activeAlertThreshold);
                boolean isDrill = alertDetails.isDrill();

                final Graphics g = this.panel.getGraphics();
                final String splitRegex = Pattern.quote(", ");
                String groupedLocs = "";
                for (String locStr : alertDetails.locations()) {
                    for (String loc : locStr.split(splitRegex)) {
                        activeAlertsDrawn |= isActive;
                        String newGroupedLocs = groupedLocs + (groupedLocs.isEmpty() ? "" : ", ") + loc;
                        int widthInPx = getWidthInPx(newGroupedLocs, g, f);
                        boolean nextLocFits = nextLocFits(maxAlertWidth, widthInPx);
                        if (groupedLocs.isEmpty() || nextLocFits) {
                            groupedLocs = newGroupedLocs;
                        } else {
                            addNextCellToPanel(tracker, false, areaColor(alertDetails, isActive), groupedLocs, g, maxAlertWidth);

                            if (tracker.isDone())
                                break DONE_WITH_ALERTS;

                            groupedLocs = loc;
                        }
                    }
                }
                if (!groupedLocs.isEmpty())
                    addNextCellToPanel(tracker, false, areaColor(alertDetails, isActive), groupedLocs);
            }
        } else {
            tracker = new GridPlaceTracker(1);
            setAlertUpdateStatus(activeAlertThreshold, tracker);
        }
        panel.revalidate();
        panel.repaint();
    }

    private static boolean nextLocFits(int maxAlertWidth, int widthInPx) {
        return maxAlertWidth >= widthInPx;
    }

    private static Color areaColor(AlertDetails alertDetails, boolean isActive) {
        if (alertDetails.isDrill())
            return Color.CYAN;
        if (alertDetails.updateFlashType() != null)
            return alertDetails.updateFlashType().getColor();

        return isActive ? Color.RED : Color.YELLOW;
    }

    private static String getDateLabel(AlertDetails alertDetails) {
        return "Date: " + alertDetails.remoteTimestamp().toLocalDate().toString();
    }

    private static String getGroupedAlertTitle(AlertDetails alertDetails) {
        return OrefDateTimeUtils.formatTimeShort(alertDetails.remoteTimestamp()) + ' ' + alertDetails.translatedCategory();
    }

    private int maxGroupedAlertTitle(List<AlertDetails> alertDetails, Font f) {
        int ret = getWidthInPx(getAlertUpdatedStr(), this.panel.getGraphics(), f);
        for (var alertDetail : alertDetails) {
            ret = Math.max(ret, getWidthInPx(getGroupedAlertTitle(alertDetail), this.panel.getGraphics(), f));
            ret = Math.max(ret, getWidthInPx(getDateLabel(alertDetail), this.panel.getGraphics(), f));
        }
        return ret + 16; // add pixels so text doesn't sit on each other
    }

    private int numActiveAlerts(LocalDateTime activeThreshold) {
        int numActiveAlerts = alertStatus.getAlerts().stream()
                .filter(ad -> ad.remoteTimestamp().isAfter(getActiveAlertThreshold()))
                .map(AlertDetails::locations)
                .mapToInt(Collection::size)
                .sum();
        return numActiveAlerts;
    }

    private void setAlertUpdateStatus(LocalDateTime activeThreshold, GridPlaceTracker tracker) {
        Color color = hasAlert(activeThreshold) ? Color.RED : Color.WHITE;
        addNextCellToPanel(tracker, true, color, getAlertUpdatedStr());
    }

    private String getAlertUpdatedStr() {
        String lastRetrievedDateStr = OrefDateTimeUtils.formatTimeShort(alertStatus.getLastUpdate());
        return "Updated: " + lastRetrievedDateStr;
    }

    public boolean hasAlert(LocalDateTime threshold) {
        return !alertStatus.getAlerts().isEmpty() && alertStatus.getAlerts().get(0).remoteTimestamp().isAfter(threshold);
    }

    public ActiveAlertState getAlertStatus(LocalDateTime alertTreshold) {
        return alertStatus.activeAlertState(alertTreshold, alertsManager.alertArea());
    }

    public ActiveAlertState getAlertStatusAllCountry(LocalDateTime alertThreshold) {
        return alertStatus.activeAlertState(alertThreshold);
    }

    public void triggerResize(int width, int height) {
        this.maxHeight = height;
        this.maxWidth = width;
        rewriteAlerts(getActiveAlertThreshold());
    }

    private static LocalDateTime getActiveAlertThreshold() {
        return LocalDateTime.now().minusMinutes(10);
    }

    private static LocalDateTime getRecentAlertTimeframeForAlertArea() {
        return LocalDateTime.now().minusHours(18);
    }

    private void addNextCellToPanel(GridPlaceTracker tracker, boolean isBold, Color color, String message) {
        addNextCellToPanel(tracker, isBold, color, message, null, NO_TARGET_WIDTH);
    }

    private void addNextCellToPanel(GridPlaceTracker tracker, boolean isBold, Color color, String message, Graphics g, int maxWidthTarget) {
        GridBagConstraints gc = new GridBagConstraints();
        int fontSize = orefConfig.getMinFontSize();
        JLabel label = createLabel(fontSize, color, isBold);

        // make sure label size does not exceed width target.
        if (g != null) {
            Font oldFont = label.getFont();
            label.setFont(
                    FontSizeUtils.findBestFontSize(g, oldFont, message, maxWidthTarget, Integer.MAX_VALUE, fontSize));
            logger.trace("Old: {} New: {}", oldFont, label.getFont());
        }
        tracker.next(label.getFontMetrics(label.getFont()).getHeight());
        if (tracker.isDone())
            return;

        gc.anchor = orefConfig.isRightToLeft() ? GridBagConstraints.EAST : GridBagConstraints.WEST;
        gc.gridx = tracker.getCol();
        gc.gridy = tracker.getRow();
        gc.weightx = 1;
        gc.weighty = 1;
        gc.fill = GridBagConstraints.BOTH;
        label.setText(message);
        panel.add(label, gc);
        infoLabels.add(label);
    }

    private JLabel createLabel(int size, Color foreground, boolean bold) {
        JLabel orefLabel = new JLabel();
        orefLabel.setHorizontalAlignment(orefConfig.isRightToLeft() ? SwingConstants.RIGHT : SwingConstants.LEFT);
        orefLabel.setFont(getFont(size, bold));
        orefLabel.setForeground(foreground);
        orefLabel.setBackground(Color.BLACK);
        return orefLabel;
    }

    private Font getFont(int size, boolean bold) {
        return new Font(panel.getFont().getName(), bold ? Font.BOLD : Font.PLAIN, size);
    }

    class GridPlaceTracker {
        private final int maxNumCols;
        private int rowsUsed = 0;
        private int heightPxUsed = 0;
        private int row = -1;
        private int col = 0;

        public GridPlaceTracker(int maxNumCols) {
            this.maxNumCols = maxNumCols;
        }

        public void next(int heightPxUsed) {
            this.heightPxUsed += heightPxUsed;
            if (PekudeiOrefView.this.maxHeight < this.heightPxUsed) {
                this.heightPxUsed = heightPxUsed;
                row = -1;
                col++;
            }
            row++;
            rowsUsed = Math.max(row, rowsUsed);
        }

        public boolean isDone() {
            return col >= maxNumCols;
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
            return orefConfig.isRightToLeft() ? maxNumCols - col : col;
        }
    }
}
