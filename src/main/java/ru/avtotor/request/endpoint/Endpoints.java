package ru.avtotor.request.endpoint;

public final class Endpoints {

    // root endpoint
    public static final String ROOT = "/";
    public static final String API = ROOT + "api";

    // perms
    public static final String PERMS = API + "/perms";
    public static final String VALIDATE_PERMISSIONS = PERMS + "/validate";
    public static final String PERMISSIONS = PERMS + "/permissions";
    public static final String UPDATE_PERMISSIONS = PERMS + "/update";


    // authorisation endpoints
    public static final String AUTH = API + "/auth";
    public static final String REGISTER = AUTH + "/register";
    public static final String LOGIN = AUTH + "/login";

    // pass
    public static final String PASS = AUTH + "/pass";
    public static final String PASS_CHANGER = PASS + "/changer";

    // user endpoint
    public static final String USER = API + "/user";

    // lock endpoints
    public static final String BLOCK = API + "/block";
    public static final String LOCK = BLOCK + "/lock";
    public static final String UNLOCK = BLOCK + "/unlock";
    public static final String LOCK_CHECK = BLOCK + "/check";
}