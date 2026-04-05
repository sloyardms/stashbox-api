package com.sloyardms.stashboxapi.shared.service;

import com.sloyardms.stashboxapi.shared.exception.types.EmptyPatchBodyException;
import com.sloyardms.stashboxapi.shared.exception.types.InvalidPatchFieldException;
import com.sloyardms.stashboxapi.shared.exception.types.InvalidPatchStructureException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.node.ObjectNode;

import java.util.Set;

@Slf4j
@Service
@AllArgsConstructor
public class JsonPatchService {

    private final ObjectMapper objectMapper;
    private final Validator validator;

    public <T> T applyPatch(JsonNode patch, T target, Class<T> targetClass) {
        if (patch == null || patch.isNull() || patch.isEmpty()) {
            throw new EmptyPatchBodyException();
        }

        JsonNode targetNode = convertToJsonNode(target);
        JsonNode patchedNode = merge(patch, targetNode);
        T result = convertToBean(patchedNode, targetClass);
        validate(result);
        return result;
    }

    private <T> JsonNode convertToJsonNode(T target) {
        try {
            return objectMapper.convertValue(target, JsonNode.class);
        } catch (IllegalArgumentException ex) {
            String message = String.format("Failed to serialize %s to JsonNode", target.getClass().getSimpleName());
            throw new IllegalStateException(message, ex);
        }
    }

    private JsonNode merge(JsonNode patch, JsonNode targetNode) {
        if (!patch.isObject()) {
            throw new InvalidPatchStructureException();
        }

        ObjectNode result = targetNode.isObject()
                ? (ObjectNode) targetNode.deepCopy()
                : objectMapper.createObjectNode();

        patch.properties().forEach(entry -> {
            if (entry.getValue().isNull()) {
                result.remove(entry.getKey());
            } else if (entry.getValue().isObject()) {
                result.set(entry.getKey(), merge(
                        entry.getValue(),
                        result.has(entry.getKey()) ? result.get(entry.getKey()) : objectMapper.createObjectNode()
                ));
            } else {
                result.set(entry.getKey(), entry.getValue().deepCopy());
            }
        });

        return result;
    }

    private <T> T convertToBean(JsonNode patch, Class<T> targetClass) {
        try {
            return objectMapper.convertValue(patch, targetClass);
        } catch (InvalidFormatException ex) {
            String value = ex.getPath().getFirst().getPropertyName();
            throw new InvalidPatchFieldException(value);
        } catch (IllegalStateException ex) {
            throw ex;
        }
    }

    private <T> void validate(T bean) {
        Set<ConstraintViolation<T>> violations = validator.validate(bean);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
