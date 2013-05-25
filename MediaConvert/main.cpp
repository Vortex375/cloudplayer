#include <iostream>
#include "MediaConvert.h"
#include "Application.h"

extern "C" {
    #include <gst/gst.h>
}

int main(int argc, char **argv) {
    // initialize gstreamer library
    gst_init(&argc, &argv);
    
    // initialize QApplication
    Application app(argc, argv);
    
    // start main loop
    return app.exec();
}
