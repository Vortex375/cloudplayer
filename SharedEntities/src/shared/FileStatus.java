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
public enum FileStatus implements Serializable, DataType {
    NOT_PREPARED,
    PREPARING,
    TRANSCODING,
    PREPARED,
    FAILED
}
