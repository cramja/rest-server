package com.cramja.crypto.web;

import com.cramja.rest.core.annotations.Path;
import com.cramja.rest.core.annotations.PathParam;
import com.cramja.rest.core.annotations.Service;
import java.util.List;

@Service
public interface ChainService {

    @Path(value = "/miners/{name}", method = "PUT")
    void createMiner(@PathParam("name") String name);

    @Path(value = "/miners/{name}", method = "DELETE")
    void deleteMiner(@PathParam("name") String name);

    @Path(value = "/miners", method = "GET")
    List<Long> listMiners();

}
