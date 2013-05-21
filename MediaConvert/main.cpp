#include <iostream>
#include <QCoreApplication>
#include "MediaConvert.h"

extern "C" {
    #include <gst/gst.h>
}

int main(int argc, char **argv) {
    // initialize gstreamer library
    gst_init(&argc, &argv);
    
    // initialize QApplication
    QCoreApplication app(&argc, argv);
    
    MediaConvert mediaConvert();
    
    // quit application on error
    QObject::connect(mediaConvert, SIGNAL(error(char*)), app, SLOT(quit()));
    
    // start main loop
    return app.exec();
}
