package com.sloyardms.stashboxapi.error;

public class MessageKeys {

    private MessageKeys() {
    }

    public static final class Error {

        public static final class Auth {

            public static final String UNAUTHORIZED_TITLE = "error.auth.unauthorized.title";
            public static final String UNAUTHORIZED_DETAIL = "error.auth.unauthorized.detail";
            public static final String FORBIDDEN_TITLE = "error.auth.forbidden.title";
            public static final String FORBIDDEN_DETAIL = "error.auth.forbidden.detail";

        }
    }
}
