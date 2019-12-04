package com.cramja.rest.core.test;

import java.util.List;
import java.util.Stack;
import java.util.UUID;

public class EntityServiceImpl implements EntityService {

    private Stack<Object> args = new Stack<>();
    private Stack<Object> returnVals = new Stack<>();

    @Override
    public Entity putEntity(UUID id, Entity entity) {
        args.push(id);
        args.push(entity);
        return (Entity) returnVals.pop();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Entity> listEntities(
            String filter,
            Integer pageSize,
            String pageToken
    ) {
        args.push(filter);
        args.push(pageSize);
        args.push(pageToken);
        return (List<Entity>) returnVals.pop();
    }

    public Object popArg() {
        return args.pop();
    }

    public void pushReturnValue(Object o) {
        returnVals.push(o);
    }

    public void clear() {
        args.clear();
        returnVals.clear();
    }
}
