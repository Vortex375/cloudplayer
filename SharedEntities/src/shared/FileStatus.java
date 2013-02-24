/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.pandaserv.music.shared;

import java.io.Serializable;

/**
 *
 * @author ich
 */
public enum FileStatus implements Serializable {
    NOT_PREPARED,
    PREPARING,
    PREPARED,
    FAILED
}
