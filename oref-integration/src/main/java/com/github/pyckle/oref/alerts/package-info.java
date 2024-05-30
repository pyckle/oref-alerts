/**
 * This package contains classes that parse the output of the services to maintain updated state of alerts and history.
 * This process is complicated because there is no primary key to merge alerts with history, and some "best effort" is
 * required to ensure alerts are not duplicated
 */
package com.github.pyckle.oref.alerts;
