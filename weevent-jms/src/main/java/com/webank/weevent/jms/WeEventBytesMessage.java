package com.webank.weevent.jms;


import java.util.Enumeration;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;

import lombok.extern.slf4j.Slf4j;

/**
 * WeEvent JMS BytesMessage.
 *
 * @author matthewliu
 * @since 2019/03/25
 */
@Slf4j
public class WeEventBytesMessage implements BytesMessage {
    private byte[] bytes;

    // custom properties in WeEvent.class
    private String eventId;
    private Map<String, String> extensions;

    // binding WeEventTopic
    private WeEventTopic weEventTopic;

    public Map<String, String> getExtensions() {
        return this.extensions;
    }

    public void setExtensions(Map<String, String> extensions) {
        this.extensions = extensions;
    }

    // BytesMessage override methods

    @Override
    public long getBodyLength() {
        if (this.bytes == null) {
            return 0;
        }

        return bytes.length;
    }

    @Override
    public boolean readBoolean() throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public byte readByte() throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public int readUnsignedByte() throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public short readShort() throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public int readUnsignedShort() throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public char readChar() throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public int readInt() throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public long readLong() throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public float readFloat() throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public double readDouble() throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public String readUTF() throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public int readBytes(byte[] bytes) throws JMSException {
        if (this.bytes == null) {
            return 0;
        }

        if (bytes.length < this.bytes.length) {
            throw new JMSException("too little buffer size");
        }
        System.arraycopy(this.bytes, 0, bytes, 0, this.bytes.length);
        return this.bytes.length;
    }

    @Override
    public int readBytes(byte[] bytes, int i) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void writeBoolean(boolean b) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void writeByte(byte b) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void writeShort(short i) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void writeChar(char c) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void writeInt(int i) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void writeLong(long l) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void writeFloat(float v) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void writeDouble(double v) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void writeUTF(String s) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void writeBytes(byte[] bytes) throws JMSException {
        this.bytes = new byte[bytes.length];
        System.arraycopy(bytes, 0, this.bytes, 0, bytes.length);
    }

    @Override
    public void writeBytes(byte[] bytes, int i, int i1) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void writeObject(Object o) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void reset() throws JMSException {
        this.bytes = null;
    }

    // Message override methods


    @Override
    public void clearBody() throws JMSException {
        this.bytes = null;
    }

    @Override
    public String getJMSMessageID() throws JMSException {
        return this.eventId;
    }

    @Override
    public void setJMSMessageID(String eventId) throws JMSException {
        this.eventId = eventId;
    }

    @Override
    public Destination getJMSDestination() throws JMSException {
        return this.weEventTopic;
    }

    @Override
    public void setJMSDestination(Destination destination) throws JMSException {
        if (destination instanceof WeEventTopic) {
            this.weEventTopic = (WeEventTopic) destination;
            return;
        }

        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public long getJMSTimestamp() throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void setJMSTimestamp(long l) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public byte[] getJMSCorrelationIDAsBytes() throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void setJMSCorrelationIDAsBytes(byte[] bytes) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void setJMSCorrelationID(String s) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public String getJMSCorrelationID() throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public Destination getJMSReplyTo() throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void setJMSReplyTo(Destination destination) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public int getJMSDeliveryMode() throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void setJMSDeliveryMode(int i) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public boolean getJMSRedelivered() throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void setJMSRedelivered(boolean b) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public String getJMSType() throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void setJMSType(String s) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public long getJMSExpiration() throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void setJMSExpiration(long l) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public int getJMSPriority() throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void setJMSPriority(int i) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void clearProperties() throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public boolean propertyExists(String s) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public boolean getBooleanProperty(String s) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public byte getByteProperty(String s) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public short getShortProperty(String s) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public int getIntProperty(String s) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public long getLongProperty(String s) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public float getFloatProperty(String s) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public double getDoubleProperty(String s) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public String getStringProperty(String s) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public Object getObjectProperty(String s) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public Enumeration getPropertyNames() throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void setBooleanProperty(String s, boolean b) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void setByteProperty(String s, byte b) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void setShortProperty(String s, short i) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void setIntProperty(String s, int i) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void setLongProperty(String s, long l) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void setFloatProperty(String s, float v) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void setDoubleProperty(String s, double v) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void setStringProperty(String s, String s1) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void setObjectProperty(String s, Object o) throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }

    @Override
    public void acknowledge() throws JMSException {
        throw new JMSException(WeEventConnectionFactory.NotSupportTips);
    }
}
