package ru.avtotor.service;

import com.sun.net.httpserver.HttpHandler;
import ru.avtotor.handlers.APIHandler;
import ru.avtotor.handlers.UnknownHandler;
import ru.avtotor.handlers.auth.login.LoginHandler;
import ru.avtotor.handlers.auth.pass.PassChangerHandler;
import ru.avtotor.handlers.auth.register.RegisterHandler;
import ru.avtotor.handlers.block.lock.LockHandler;
import ru.avtotor.handlers.block.lockCheck.LockCheckHandler;
import ru.avtotor.handlers.block.unlock.UnlockHandler;
import ru.avtotor.handlers.token.update.UpdateHandler;
import ru.avtotor.handlers.user.permissions.PermissionsHandler;
import ru.avtotor.handlers.user.user.UserHandler;
import ru.avtotor.handlers.user.validate.ValidateHandler;
import ru.avtotor.request.endpoint.Endpoint;
import ru.avtotor.request.endpoint.Endpoints;
import ru.avtotor.request.method.HttpMethod;
import ru.avtotor.request.params.Params;

public class HandlerService {

    @Endpoint(endpoint = Endpoints.ROOT, method = HttpMethod.ANY)
    public static HttpHandler unknown(String endpoint, byte method, Params params) {
        return new UnknownHandler(endpoint, method, params);
    }

    @Endpoint(endpoint = Endpoints.API, method = HttpMethod.ANY)
    public static HttpHandler api(String endpoint, byte method, Params params) {
        return new APIHandler(endpoint, method, params);
    }

    @Endpoint(endpoint = Endpoints.REGISTER, method = HttpMethod.POST)
    @Params(required = {"surname", "name", "middleName", "phone", "login", "password"},
            optional = {"address", "sex"})
    public static HttpHandler register(String endpoint, byte method, Params params) {
        return new RegisterHandler(endpoint, method, params);
    }

    @Endpoint(endpoint = Endpoints.LOGIN, method = HttpMethod.POST)
    @Params(required = {"login", "password"})
    public static HttpHandler login(String endpoint, byte method, Params params) {
        return new LoginHandler(endpoint, method, params);
    }

    @Endpoint(endpoint = Endpoints.PASS_CHANGER, method = HttpMethod.POST)
    @Params(required = {"newPassword"},
            variable = {"token", "login, oldPassword"})
    public static HttpHandler passChanger(String endpoint, byte method, Params params) {
        return new PassChangerHandler(endpoint, method, params);
    }

    @Endpoint(endpoint = Endpoints.PERMISSIONS, method = HttpMethod.ANY)
    public static HttpHandler permissions(String endpoint, byte method, Params params) {
        return new PermissionsHandler(endpoint, method, params);
    }

    @Endpoint(endpoint = Endpoints.VALIDATE_PERMISSIONS, method = HttpMethod.POST)
    @Params(required = {"perms"},
            variable = {"userID", "login", "token"})
    public static HttpHandler validate(String endpoint, byte method, Params params) {
        return new ValidateHandler(endpoint, method, params);
    }

    @Endpoint(endpoint = Endpoints.UPDATE_PERMISSIONS, method = HttpMethod.POST)
    @Params(required = {"perms", "managerToken"},
            variable = {"userID", "login", "token"})
    public static HttpHandler update(String endpoint, byte method, Params params) {
        return new UpdateHandler(endpoint, method, params);
    }

    @Endpoint(endpoint = Endpoints.LOCK_CHECK, method = HttpMethod.POST)
    @Params(variable = {"userID", "login", "token"})
    public static HttpHandler check(String endpoint, byte method, Params params) {
        return new LockCheckHandler(endpoint, method, params);
    }

    @Endpoint(endpoint = Endpoints.LOCK, method = HttpMethod.POST)
    @Params(required = {"expires", "reason", "lockerToken"},
            variable = {"userID", "login", "token"})
    public static HttpHandler lock(String endpoint, byte method, Params params) {
        return new LockHandler(endpoint, method, params);
    }

    @Endpoint(endpoint = Endpoints.UNLOCK, method = HttpMethod.POST)
    @Params(required = {"lockerToken"},
            optional = {"all"},
            variable = {"userID", "login", "token"})
    public static HttpHandler unlock(String endpoint, byte method, Params params) {
        return new UnlockHandler(endpoint, method, params);
    }

    @Endpoint(endpoint = Endpoints.USER, method = HttpMethod.POST)
    @Params(required = {"readerToken"},
            variable = {"userID", "login", "token"})
    public static HttpHandler user(String endpoint, byte method, Params params) {
        return new UserHandler(endpoint, method, params);
    }
}
