package com.ragin.bdd.cucumber.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for logging.
 * <p>
 *     This class provides a variable LOG, which is an instance of {@link org.apache.commons.logging.Log} for
 *     classes which need to log.
 * </p>
 */
public abstract class Loggable {
    protected static final Log LOG = LogFactory.getLog("cucumberTest");
}
