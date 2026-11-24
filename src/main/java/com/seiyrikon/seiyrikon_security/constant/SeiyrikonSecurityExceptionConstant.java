package com.seiyrikon.seiyrikon_security.constant;

public class SeiyrikonSecurityExceptionConstant {
    //error code
    public static final String AUTH_01 = "AUTH01";
    public static final String AUTH_02 = "AUTH02";
    public static final String AUTH_03 = "AUTH03";
    public static final String AUTH_04 = "AUTH04";
    public static final String AUTH_05 = "AUTH05";
    public static final String AUTH_06 = "AUTH06";
    public static final String AUTH_07 = "AUTH07";
    public static final String AUTH_08 = "AUTH08";
    public static final String AUTH_09 = "AUTH09";
    public static final String AUTH_10 = "AUTH10";

    //error message
    public static final String ERR_MESSAGE_01 = "User not found.";
    public static final String ERR_MESSAGE_02 = "Invalid credentials.";
    public static final String ERR_MESSAGE_03 = "User don't have any roles assigned.";
    public static final String ERR_MESSAGE_04 = "No token provided or is not a Bearer token.";
    public static final String ERR_MESSAGE_05 = "The token provided is blacklisted.";
    public static final String ERR_MESSAGE_06 = "Expired token.";
    public static final String ERR_MESSAGE_07 = "No authenticated user found.";
    public static final String ERR_MESSAGE_08 = "Invalid principal type.";
    public static final String ERR_MESSAGE_09 = "Unauthenticated user.";
    public static final String ERR_MESSAGE_10 = "Please login again.";
}
