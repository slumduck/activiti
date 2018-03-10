package com.mime.demo.activiti.modeler.controller.editor;

import org.activiti.engine.ActivitiException;
import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;

/**
 * @author SlumDuck
 * @create 2018-03-09 15:05
 * @desc
 */
@RestController
@RequestMapping("service")
public class StencilsetRestResource {
    public StencilsetRestResource() {
    }

    @RequestMapping(
            value = {"/editor/stencilset"},
            method = {RequestMethod.GET},
            produces = {"application/json;charset=utf-8"}
    )
    @ResponseBody
    public String getStencilset() {
        InputStream stencilsetStream = this.getClass().getClassLoader().getResourceAsStream("stencilset.json");

        try {
            return IOUtils.toString(stencilsetStream, "utf-8");
        } catch (Exception var3) {
            throw new ActivitiException("Error while loading stencil set", var3);
        }
    }
}
