package com.mime.demo.activiti.diagram;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author SlumDuck
 * @create 2018-03-09 15:00
 * @desc
 */
@RestController
@RequestMapping("/service")
public class ProcessDefinitionDiagramLayoutResource extends BaseProcessDefinitionDiagramLayoutResource {
    public ProcessDefinitionDiagramLayoutResource() {
    }

    @RequestMapping(
            value = {"/process-definition/{processDefinitionId}/diagram-layout"},
            method = {RequestMethod.GET},
            produces = {"application/json"}
    )
    public ObjectNode getDiagram(@PathVariable String processDefinitionId) {
        return this.getDiagramNode((String)null, processDefinitionId);
    }
}
