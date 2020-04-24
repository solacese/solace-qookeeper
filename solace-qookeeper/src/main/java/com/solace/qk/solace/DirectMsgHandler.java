package com.solace.qk.solace;

import com.solacesystems.jcsmp.BytesXMLMessage;

public interface DirectMsgHandler {
    void onDirectMessage(BytesXMLMessage message);
}
