package ai.flowx.integration.repository.utils;

import ai.flowx.integration.dto.enums.ParamType;

import java.util.Map;

public class EndpointFieldNames {

    public static final String ID = "id";
    public static final String MODIFIED_DATE = "modifiedDate";
    public static final String MODIFIED_BY = "modifiedBy";


    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String URL = "url";
    public static final String HTTP_METHOD = "httpMethod";
    public static final String PAYLOAD = "payload";

    public static final String RESPONSES = "responses";
    public static final String RESPONSES_ID = "responses.id";
    public static final String RESPONSES_$ = "responses.$";

    public static final Map<ParamType, String> PARAM_NAMES_BY_PARAM_TYPE = Map.of(ParamType.HEADER, "headers", ParamType.QUERY, "queryParameters", ParamType.PATH, "pathParameters");
    public static final Map<ParamType, String> PARAM_NAMES_$_BY_PARAM_TYPE = Map.of(ParamType.HEADER, "headers.$", ParamType.QUERY, "queryParameters.$", ParamType.PATH, "pathParameters.$");
    public static final Map<ParamType, String> PARAM_IDS_BY_PARAM_TYPE = Map.of(ParamType.HEADER, "headers.id", ParamType.QUERY, "queryParameters.id", ParamType.PATH, "pathParameters.id");

    EndpointFieldNames() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
