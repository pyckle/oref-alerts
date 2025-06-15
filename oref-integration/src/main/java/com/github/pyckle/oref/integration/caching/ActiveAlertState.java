package com.github.pyckle.oref.integration.caching;

import com.github.pyckle.oref.integration.translationstores.UpdateFlashType;

public record ActiveAlertState(boolean hasAlert, UpdateFlashType mostSevereUpdateOrFlashType)
{
    public boolean hasNonFlashOrUpdate()
    {
        return mostSevereUpdateOrFlashType != null;
    }
}
