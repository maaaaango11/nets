package global;

public interface GlobalVariables {
    byte SOCKS5_VERSION = 0x05;
    byte MIN_AUTH_NUMBER = 0x01;
    byte NO_AUTH = 0x00;
    byte IPV4_ADDRESS = 0x01;
    byte DOMAIN_NAME = 0x03;
    byte TCP_STREAM_CONNECTION = 0x01;
    byte RESERVED = 0x00;
    byte REQUEST_GRANTED = 0x00;
    int MIN_PORT = 0;
    int MAX_PORT = 0xFFFF;
    int MAX_GREETING_MESSAGE_LENGTH = 257;
    int MAX_CONNECTION_MESSAGE_LENGTH = 262;
}
