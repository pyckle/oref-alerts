package com.github.pyckle.oref.ui;

import com.github.pyckle.oref.integration.activealerts.ActiveAlert;
import com.github.pyckle.oref.integration.activealerts.AlertTimestamps;
import com.github.pyckle.oref.integration.caching.CachedApiResult;
import com.github.pyckle.oref.integration.caching.OrefApiCachingService;
import com.github.pyckle.oref.integration.config.OrefConfig;
import com.github.pyckle.oref.integration.datetime.OrefDateTimeUtils;
import com.github.pyckle.oref.integration.district.DistrictStore;
import com.github.pyckle.oref.integration.dto.HistoryEventWithParsedDates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PekudeiOrefView {
    private static final Logger logger = LoggerFactory.getLogger(PekudeiOrefView.class);

    private final OrefConfig orefConfig;

    private final JPanel panel;
    private final List<JLabel> infoLabels = new ArrayList<>();
    private final OrefApiCachingService orefApiCachingService;
    private List<HistoryEventWithParsedDates> lastHistoryUpdate = List.of();
    private CachedApiResult<List<ActiveAlert>> mostRecentAlert = CachedApiResult.getUninitializedResult();
    private Instant lastDistrictStoreUpdate = null;
    private int numActiveAlerts = 0;

    private int maxHeight;
    private int maxWidth;
    private int clockHeight;


    public PekudeiOrefView(OrefConfig orefConfig) {
        this.orefConfig = orefConfig;
        this.orefApiCachingService = new OrefApiCachingService(orefConfig);
        panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
    }

    public JPanel getPanel() {
        return panel;
    }

    public boolean update(Instant now) {
        try {
            boolean oldHasAlert = hasAlert();
            internalUpdateEvents();
            return oldHasAlert != hasAlert();
        } catch (Exception ex) {
            // don't do anything - rest of clock should stay working.
            logger.error("error updating oref view", ex);
        }
        return false;
    }

    public void internalUpdateEvents() {
        var historyResult = this.orefApiCachingService.getHistory();
        var alertResult = this.orefApiCachingService.getAlert();
        var districtStore = this.orefApiCachingService.getDistrictApi();

        // if the alert last retrieved date changed but the alert is identical, update only the last time alerts
        // were fetched, no need to update anything else
        if (!alertResult.lastRetrieved().equals(mostRecentAlert.lastRetrieved()) &&
                Objects.equals(alertResult.retrievedValue(), mostRecentAlert.retrievedValue())) {
            mostRecentAlert = alertResult;
            if (!this.infoLabels.isEmpty()) {
                this.infoLabels.get(0).setText(getAlertUpdatedStr());
            }
        }

        // if the history value retrieved is the same, as well as the alert results and districts,
        // no need to rebuild the page.
        if (historyResult.retrievedValue() == lastHistoryUpdate
                && Objects.equals(mostRecentAlert.retrievedValue(), alertResult.retrievedValue())
                && lastDistrictStoreUpdate.equals(districtStore.lastRetrieved())) {
            // no new info
            return;
        }
        // update everything
        lastHistoryUpdate = historyResult.retrievedValue();
        mostRecentAlert = alertResult;
        lastDistrictStoreUpdate = districtStore.lastRetrieved();

        List<HistoryEventWithParsedDates> historyEvents = Objects.requireNonNullElse(lastHistoryUpdate, List.of());

        panel.removeAll();
        infoLabels.clear();

        GridPlaceTracker tracker = new GridPlaceTracker();
        LocalDate lastEventDate = null;
        LocalTime lastEventTime = null;

        updateAlertState(alertResult);
        addAlertMessages(tracker);
        addHistoryToView(historyResult, tracker, historyEvents, lastEventDate, lastEventTime);
        padGridViewWithEmptyLabels(tracker);

        // as we added different strings, we may need a different font size
        triggerResize();
    }

    private void padGridViewWithEmptyLabels(GridPlaceTracker tracker) {
        // add empty labels so gridbaglayout clears the junk that was there. Maybe there's a better way to do it?
        while (tracker.numRowsRemainingInCol() > 0) {
            addNextCellToPanel(tracker, false, Color.BLACK, "");
        }
    }

    private void addHistoryToView(CachedApiResult<List<HistoryEventWithParsedDates>> historyResult,
                                  GridPlaceTracker tracker,
                                  List<HistoryEventWithParsedDates> historyEvents, LocalDate lastEventDate,
                                  LocalTime lastEventTime) {
        String lastUpdatedHistory = OrefDateTimeUtils.formatDateAndTimeShort(historyResult.lastRetrieved());
        addNextCellToPanel(tracker, true, Color.WHITE, "Updated: " + lastUpdatedHistory);
        addNextCellToPanel(tracker, true, !historyEvents.isEmpty() ? Color.RED : Color.WHITE,
                "Events in Past 24 hrs: " + historyEvents.size());
        String lastEventCategory = "";
        for (var event : historyEvents) {
            if (!Objects.equals(event.date(), lastEventDate)) {
                addNextCellToPanel(tracker, true, Color.PINK, "Date: " + event.date());
                lastEventDate = event.date();
            }

            if (tracker.isDone()) break;

            if (!Objects.equals(event.time(), lastEventTime) ||
                    !Objects.equals(event.category_desc(), lastEventCategory)) {
                addNextCellToPanel(tracker, true, Color.YELLOW, event.time() + " " + event.category_desc());
                lastEventTime = event.time();
                lastEventCategory = event.category_desc();
            }
            if (tracker.isDone()) break;
            addNextCellToPanel(tracker, false, Color.ORANGE, event.data());
            if (tracker.isDone()) break;
        }
    }

    private void updateAlertState(CachedApiResult<List<ActiveAlert>> alertResult) {
        int oldActiveAlerts = this.numActiveAlerts;
        var alerts = alertResult.retrievedValue();

        boolean hasAlert = hasAlert();
        this.numActiveAlerts = !hasAlert ? 0 : alerts.stream()
                .mapToInt(ActiveAlert::numDisplayedAreas)
                .sum();
        if (numActiveAlerts == 0 && hasAlert) {
            logger.warn("Required data field in alerts object is absent {}", alerts);
        }
        if (numActiveAlerts != oldActiveAlerts) {
            logger.info("Active Alerts {}", numActiveAlerts);
        }
    }

    private void addAlertMessages(GridPlaceTracker tracker) {
        if (hasAlert()) {
            var alerts = mostRecentAlert.retrievedValue();

            addNextCellToPanel(tracker, true, Color.RED, getAlertUpdatedStr());
            for (var alert : alerts) {
                addAlert(tracker, alert);
            }
        } else {
            addNextCellToPanel(tracker, true, Color.WHITE, getAlertUpdatedStr());
        }
    }

    private void addAlert(GridPlaceTracker tracker, ActiveAlert activeAlert) {
        AlertTimestamps alertTimestamps = activeAlert.alertTimestamps();
        addNextCellToPanel(tracker, true, alertTimestamps.isDelayReasonable() ? Color.RED : Color.CYAN,
                alertTimestamps.getDecodedTimestampStr());

        // hopefully we don't get here - it means that an alert was delayed significantly. If it is,
        // we add the received timestamp and color it differently to indicate this may not be real.
        if (!alertTimestamps.isDelayReasonable()) {
            addNextCellToPanel(tracker, true, Color.CYAN, "Received: " + alertTimestamps.getReceivedTimestampStr());
        }

        addNextCellToPanel(tracker, true, Color.RED, activeAlert.alertCategoryHeb());

        DistrictStore districtStore =
                Objects.requireNonNullElseGet(orefApiCachingService.getDistrictApi().retrievedValue(),
                        DistrictStore::new);
        List<String> translatedSortedAreas = translateAndSortAreas(activeAlert, districtStore);
        for (String translatedAlert : translatedSortedAreas) {
            addNextCellToPanel(tracker, true, Color.RED, translatedAlert);
        }
    }

    private static List<String> translateAndSortAreas(ActiveAlert activeAlert, DistrictStore districtStore) {
        List<String> translatedSortedAreas = activeAlert.filteredAreasToDisplay().stream()
                .map(districtStore::getTranslationFromHebrewLabel)
                .sorted()
                .toList();
        return translatedSortedAreas;
    }

    private String getAlertUpdatedStr() {
        String lastRetrievedDateStr = OrefDateTimeUtils.formatDateAndTimeShort(mostRecentAlert.lastRetrieved());
        return hasAlert() ?
                "Alerts as of: " + lastRetrievedDateStr
                : "No Alerts as of: " + lastRetrievedDateStr;
    }

    public boolean hasAlert() {
        List<ActiveAlert> alerts = mostRecentAlert.retrievedValue();
        return alerts != null && !alerts.isEmpty();
    }

    public void triggerResize(int width, int height, int clockHeight) {
        this.maxHeight = height;
        this.maxWidth = width;
        this.clockHeight = clockHeight;
        triggerResize();
    }

    private void triggerResize() {
        if (this.infoLabels.isEmpty() || this.infoLabels.get(0).getText().isEmpty()) {
            return;
        }

        Font newFont = null;

        int numCols = (int) Math.ceil((double) this.infoLabels.size() / orefConfig.getNumRows());
        final int maxLabelWidth = this.maxWidth / numCols;
        final int maxLabelHeight = this.maxHeight / orefConfig.getNumRows();
        for (JLabel l : infoLabels) {
            int maxFontSize = newFont == null ? Integer.MAX_VALUE : newFont.getSize();
            Font bestFontForRow = ResizeUtils.findBestFontSize(panel.getGraphics(), l.getFont(), l.getText(),
                    maxLabelWidth, maxLabelHeight, maxFontSize);
            if (newFont == null || bestFontForRow.getSize() < newFont.getSize()) {
                newFont = bestFontForRow;
            }
        }
        setNewFont(newFont.getSize());
    }

    private void setNewFont(int fontSize) {
        infoLabels.forEach(l -> l.setFont(ResizeUtils.cloneFont(l.getFont(), fontSize)));
    }

    private void addNextCellToPanel(GridPlaceTracker tracker, boolean isBold, Color color, String message) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = orefConfig.isRightToLeft() ? GridBagConstraints.EAST : GridBagConstraints.WEST;
        gc.gridx = tracker.getCol();
        gc.gridy = tracker.getRow();
        gc.weightx = 1;
        gc.weighty = 1;
        gc.fill = GridBagConstraints.BOTH;
        JLabel label = createLabel(13, color, isBold);
        label.setText(message);
        panel.add(label, gc);
        infoLabels.add(label);
        tracker.next();
    }

    private JLabel createLabel(int size, Color foreground, boolean bold) {
        JLabel orefLabel = new JLabel();
        orefLabel.setHorizontalAlignment(orefConfig.isRightToLeft() ? SwingConstants.RIGHT : SwingConstants.LEFT);
        orefLabel.setFont(new Font(orefLabel.getFont().getName(), bold ? Font.BOLD : Font.PLAIN, size));
        orefLabel.setForeground(foreground);
        orefLabel.setBackground(Color.BLACK);
        return orefLabel;
    }

    class GridPlaceTracker {
        private int row;
        private int col;

        public void next() {
            row++;
            if (row >= orefConfig.getNumRows()) {
                row = 0;
                col++;
            }
        }

        public boolean isDone() {
            return col >= orefConfig.getNumCols();
        }

        public int numRowsRemainingInCol() {
            if (isDone() || row == 0) return 0;
            return orefConfig.getNumRows() - row;
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
            return orefConfig.isRightToLeft() ? orefConfig.getNumCols() - col : col;
        }
    }
}