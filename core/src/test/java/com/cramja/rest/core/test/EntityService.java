package com.cramja.rest.core.test;

import com.cramja.rest.core.annotations.Body;
import com.cramja.rest.core.annotations.Path;
import com.cramja.rest.core.annotations.PathParam;
import com.cramja.rest.core.annotations.QueryParam;
import com.cramja.rest.core.annotations.ResponseCode;
import com.cramja.rest.core.annotations.Service;
import java.util.List;
import java.util.UUID;

@Service
@Path("/entities")
public interface EntityService {

    @Path(method = "put", value = "/{id}")
    @ResponseCode(201)
    Entity putEntity(
            @PathParam("id") UUID id,
            @Body Entity entity
    );

    @Path
    List<Entity> listEntities(
            @QueryParam("filter") String filter,
            @QueryParam("pageSize") Integer pageSize,
            @QueryParam("pageToken") String pageToken
    );
}
