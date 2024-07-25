package ai.flowx.integration.service;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtilsBean;

@Slf4j
@UtilityClass
public class PropertyUtils {

    private static final PropertyUtilsBean PROPERTY_UTILS_BEAN = new PropertyUtilsBean();

    public static Object getProperty(Object values, String fieldName) {
        try {
            return PROPERTY_UTILS_BEAN.getProperty(values, fieldName);
        } catch (Exception e) {
            log.debug("Error while getting property: {}, errorMessage: {}", fieldName, e.getMessage());
            return null;
        }
    }
}
