#include <iostream>
#include <signal.h>
#include <assert.h>

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
    
    // register signal handler for ctrl+c
    signal(SIGINT, &(app.signal_handler));
    
    // start main loop
    //std::cout << "starting main loop";
    return app.exec();
}
