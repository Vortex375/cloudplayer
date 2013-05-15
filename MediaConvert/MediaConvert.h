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

class MediaConvert : public QObject{
Q_OBJECT


public:
    MediaConvert() throw (InitException);
    virtual ~MediaConvert();
    
    
signals:
    error(char* msg);

//protected:
    
private:
    static const char* GST_PIPELINE = "fdsrc name=src ! decodebin name=decode
            " ! queue ! audioconvert ! audioresample ! vorbisenc quality=0.6" //TODO: make quality configurable
            " ! webmmux name=mux streamable=true ! fdsink name=sink";
    
    GstElement* mPipeline;
    
    static gboolean busCall(GstBus *bus, GstMessage *msg, gpointer data);
    emitError(char* msg);
    
};

#endif // MEDIA_CONVERT_H
