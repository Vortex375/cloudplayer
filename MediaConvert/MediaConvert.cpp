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

MediaConvert::MediaConvert() throw (InitException)
{
    GError* error;

    mPipeline = gst_parse_launch(MEDIACONVERT_TRANSCODE_PIPELINE, &error);

    if (!mPipeline) {
        // pipeline construction failed
        throw InitException("Unable to construct pipeline");
    }


}
MediaConvert::~MediaConvert()
{

}

void MediaConvert::emitError(char* msg)
{
  emit error(msg);
}

void MediaConvert::pause()
{
  gst_element_set_state(mPipeline, GST_STATE_PAUSED);
}

void MediaConvert::play()
{
  gst_element_set_state(mPipeline, GST_STATE_PLAYING);
}

void MediaConvert::reset()
{
  // reset pipeline
  gst_element_set_state(mPipeline, GST_STATE_NULL);
  
  // free the current pipeline and construct a new one, just to be safe
  gst_object_unref(mPipeline);
  
  GError* error;

  mPipeline = gst_parse_launch(MEDIACONVERT_TRANSCODE_PIPELINE, &error);
  
  if (!mPipeline) {
    emitError("unable to reset pipeline");
  }
}

void MediaConvert::seek(double seconds)
{

}


#include "MediaConvert.moc"
