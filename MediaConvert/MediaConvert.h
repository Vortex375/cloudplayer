/*
    <one line to give the program's name and a brief idea of what it does.>
    Copyright (C) 2012  Benjamin Schmitz <benni@wolpzone.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/


#ifndef MEDIA_CONVERT_H
#define MEDIA_CONVERT_H

#include <QtCore/QObject>

extern "C" {
    #include <gst/gst.h>
}

#include "InitException.h"

//TODO: make quality configurable
#define MEDIACONVERT_TRANSCODE_PIPELINE "filesrc name=src ! decodebin name=decode" \
            " ! queue name=queue ! audioconvert name=conv ! audioresample ! tee name=t ! queue ! pulsesink t. ! queue name=encQueue ! fdsink name=encSink"
//#define MEDIACONVERT_TRANSCODE_PIPELINE "filesrc name=src ! decodebin name=decode ! queue name=queue ! audioconvert name=conv ! audioresample ! pulsesink"


class MediaConvert : public QObject{
Q_OBJECT


public:
    MediaConvert(char* infile) throw (InitException);
    virtual ~MediaConvert();
    
    void reset();
    void pause();
    void play();
    
    void seek(double seconds);
    
    
signals:
    void error(char* msg);

//protected:
    
private:
    char* infile;
    GstElement* mPipeline;
    GstBin* encodeBin;
    guint mBusWatch;
    
    bool seekPending;
    double seekPos;
    
    static gboolean busCall(GstBus *bus, GstMessage *msg, gpointer data);
    void emitError(char* msg);
    
};

#endif // MEDIA_CONVERT_H
