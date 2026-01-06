package com.sdk.glassessdksample.web

import org.json.JSONObject

/**
 * Event classes for communication between WebCommunicationService and MainActivity
 */
class GlassesCommandEvent(val command: String, val params: JSONObject?)

class GlassesRequestEvent(val request: String, val params: JSONObject?)

class GlassesDataEvent(val dataType: String, val data: Any?)

class GlassesStatusEvent(val status: String, val value: Any?)