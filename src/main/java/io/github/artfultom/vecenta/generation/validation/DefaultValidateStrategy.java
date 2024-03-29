package io.github.artfultom.vecenta.generation.validation;

import io.github.artfultom.vecenta.exceptions.ValidateException;
import io.github.artfultom.vecenta.generation.Data;
import io.github.artfultom.vecenta.matcher.TypeConverter;
import io.github.artfultom.vecenta.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultValidateStrategy implements ValidateStrategy {

    private static final int MAX_DEPTH_OF_RECURSION = 5;

    @Override
    public void check(String fileName) throws ValidateException {
        if (fileName == null || fileName.isEmpty()) {
            throw new ValidateException("File name is empty.");
        }
        String[] words = fileName.split("\\.");
        if (words.length != 3) {
            throw new ValidateException(String.format("Incorrect file name: %s. It must have tree parts.", fileName));
        }
        if (words[0].isEmpty()) {
            throw new ValidateException(String.format("Incorrect file name: %s. Server name is empty.", fileName));
        }
        if (words[1].isEmpty()) {
            throw new ValidateException(String.format("Incorrect file name: %s. Version is empty.", fileName));
        }
        try {
            Integer.parseInt(words[1]);
        } catch (NumberFormatException e) {
            throw new ValidateException(String.format("Incorrect file name: %s. Version is incorrect.", fileName));
        }
        if (!words[2].equalsIgnoreCase("json") && !words[2].equalsIgnoreCase("yml")) {
            throw new ValidateException(String.format("Incorrect file name: %s. It must be json or yml.", fileName));
        }
    }

    @Override
    public void check(Data data) throws ValidateException {
        for (Data.Client client : data.getClients()) {
            for (Data.Entity entity : client.getEntities()) {
                checkUniqueModel(entity);
                checkMethods(entity);
                checkFields(entity);
                checkRecursion(entity);
            }
        }
    }

    private void checkUniqueModel(Data.Entity entity) throws ValidateException {
        Set<String> modelNames = entity.getModels().stream()
                .map(Data.Entity.Model::getName)
                .filter(item -> TypeConverter.get(item) == null)
                .collect(Collectors.toSet());

        if (modelNames.size() < entity.getModels().size()) {
            throw new ValidateException("Duplicates of models.");
        }
    }

    private void checkMethods(Data.Entity entity) throws ValidateException {
        Set<String> modelNames = entity.getModels().stream()
                .map(Data.Entity.Model::getName)
                .filter(item -> TypeConverter.get(item) == null)
                .collect(Collectors.toSet());

        for (Data.Entity.Method method : entity.getMethods()) {
            String returnType = method.getOut();
            checkType(returnType, modelNames);

            for (Data.Entity.Param param : method.getIn()) {
                checkType(param.getType(), modelNames);
            }
        }
    }

    private void checkFields(Data.Entity entity) throws ValidateException {
        Set<String> modelNames = entity.getModels().stream()
                .map(Data.Entity.Model::getName)
                .filter(item -> TypeConverter.get(item) == null)
                .collect(Collectors.toSet());

        for (Data.Entity.Model model : entity.getModels()) {
            for (Data.Entity.Param param : model.getFields()) {
                checkType(param.getType(), modelNames);
            }
        }
    }

    private void checkRecursion(Data.Entity entity) throws ValidateException {
        Map<String, Data.Entity.Model> modelMap = entity.getModels().stream()
                .collect(Collectors.toMap(
                        Data.Entity.Model::getName,
                        item -> item
                ));

        List<Data.Entity.Model> models = entity.getModels();
        for (int i = 0; i < MAX_DEPTH_OF_RECURSION; i++) {
            models = models.stream()
                    .map(Data.Entity.Model::getFields)
                    .flatMap(Collection::stream)
                    .map(item -> StringUtils.getSimpleTypes(item.getType()))
                    .flatMap(Collection::stream)
                    .filter(item -> TypeConverter.get(item) == null)
                    .map(modelMap::get)
                    .collect(Collectors.toList());
        }
        if (!models.isEmpty()) {
            throw new ValidateException("There is a circle!");
        }
    }

    private void checkType(String type, Set<String> modelNames) throws ValidateException {
        for (String name : StringUtils.getSimpleTypes(type)) {
            if (TypeConverter.get(name) == null && !modelNames.contains(name)) {
                throw new ValidateException(String.format("Incorrect type %s.", type));
            }
        }
    }
}
