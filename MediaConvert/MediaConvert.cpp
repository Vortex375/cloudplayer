#include "MediaConvert.h"

gboolean MediaConvert::busCall(GstBus* bus, GstMessage* msg, gpointer data)
{
    MediaConvert* that = (MediaConvert *) data;

    switch (GST_MESSAGE_TYPE (msg)) {

    case GST_MESSAGE_ERROR: {
        gchar  *debug;
        GError *error;

        gst_message_parse_error (msg, &error, &debug);
        g_free (debug);

        g_printerr ("Error: %s\n", error->message);
        that->emitError(error->message);
        g_error_free (error);
        break;
    }
    default:
        break;
    }

    return TRUE;
}

MediaConvert::MediaConvert()
{
    GError* error;

    mPipeline = gst_parse_launch(GST_PIPELINE, &error);

    if (!mPipeline) {
        // pipeline construction failed
        throw InitException("Unable to construct pipeline");
    }


}
MediaConvert::~MediaConvert()
{

}

#include "MediaConvert.moc"
