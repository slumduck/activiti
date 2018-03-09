package com.mime.demo.activiti.diagram;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.impl.bpmn.behavior.BoundaryEventActivityBehavior;
import org.activiti.engine.impl.bpmn.behavior.CallActivityBehavior;
import org.activiti.engine.impl.bpmn.parser.ErrorEventDefinition;
import org.activiti.engine.impl.bpmn.parser.EventSubscriptionDeclaration;
import org.activiti.engine.impl.jobexecutor.TimerDeclarationImpl;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.process.*;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * @author SlumDuck
 * @create 2018-03-09 14:59
 * @desc
 */
public class BaseProcessDefinitionDiagramLayoutResource {
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private HistoryService historyService;

    public BaseProcessDefinitionDiagramLayoutResource() {
    }

    public ObjectNode getDiagramNode(String processInstanceId, String processDefinitionId) {
        List<String> highLightedFlows = Collections.emptyList();
        List<String> highLightedActivities = Collections.emptyList();
        Map<String, ObjectNode> subProcessInstanceMap = new HashMap();
        ProcessInstance processInstance = null;
        if(processInstanceId != null) {
            processInstance = (ProcessInstance)this.runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            if(processInstance == null) {
                throw new ActivitiObjectNotFoundException("Process instance could not be found");
            }

            processDefinitionId = processInstance.getProcessDefinitionId();
            List<ProcessInstance> subProcessInstances = this.runtimeService.createProcessInstanceQuery().superProcessInstanceId(processInstanceId).list();
            Iterator var8 = subProcessInstances.iterator();

            while(var8.hasNext()) {
                ProcessInstance subProcessInstance = (ProcessInstance)var8.next();
                String subDefId = subProcessInstance.getProcessDefinitionId();
                String superExecutionId = ((ExecutionEntity)subProcessInstance).getSuperExecutionId();
                ProcessDefinitionEntity subDef = (ProcessDefinitionEntity)this.repositoryService.getProcessDefinition(subDefId);
                ObjectNode processInstanceJSON = (new ObjectMapper()).createObjectNode();
                processInstanceJSON.put("processInstanceId", subProcessInstance.getId());
                processInstanceJSON.put("superExecutionId", superExecutionId);
                processInstanceJSON.put("processDefinitionId", subDef.getId());
                processInstanceJSON.put("processDefinitionKey", subDef.getKey());
                processInstanceJSON.put("processDefinitionName", subDef.getName());
                subProcessInstanceMap.put(superExecutionId, processInstanceJSON);
            }
        }

        if(processDefinitionId == null) {
            throw new ActivitiObjectNotFoundException("No process definition id provided");
        } else {
            ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity)this.repositoryService.getProcessDefinition(processDefinitionId);
            if(processDefinition == null) {
                throw new ActivitiException("Process definition " + processDefinitionId + " could not be found");
            } else {
                ObjectNode responseJSON = (new ObjectMapper()).createObjectNode();
                JsonNode pdrJSON = this.getProcessDefinitionResponse(processDefinition);
                if(pdrJSON != null) {
                    responseJSON.put("processDefinition", pdrJSON);
                }

                ArrayNode laneSetArray;
                ArrayNode activityArray;
                Iterator var29;
                if(processInstance != null) {
                    laneSetArray = (new ObjectMapper()).createArrayNode();
                    activityArray = (new ObjectMapper()).createArrayNode();
                    highLightedActivities = this.runtimeService.getActiveActivityIds(processInstanceId);
                    highLightedFlows = this.getHighLightedFlows(processInstanceId, processDefinition);
                    var29 = highLightedActivities.iterator();

                    String flow;
                    while(var29.hasNext()) {
                        flow = (String)var29.next();
                        laneSetArray.add(flow);
                    }

                    var29 = highLightedFlows.iterator();

                    while(var29.hasNext()) {
                        flow = (String)var29.next();
                        activityArray.add(flow);
                    }

                    responseJSON.put("highLightedActivities", laneSetArray);
                    responseJSON.put("highLightedFlows", activityArray);
                }

                if(processDefinition.getParticipantProcess() != null) {
                    ParticipantProcess pProc = processDefinition.getParticipantProcess();
                    ObjectNode participantProcessJSON = (new ObjectMapper()).createObjectNode();
                    participantProcessJSON.put("id", pProc.getId());
                    if(StringUtils.isNotEmpty(pProc.getName())) {
                        participantProcessJSON.put("name", pProc.getName());
                    } else {
                        participantProcessJSON.put("name", "");
                    }

                    participantProcessJSON.put("x", pProc.getX());
                    participantProcessJSON.put("y", pProc.getY());
                    participantProcessJSON.put("width", pProc.getWidth());
                    participantProcessJSON.put("height", pProc.getHeight());
                    responseJSON.put("participantProcess", participantProcessJSON);
                }

                if(processDefinition.getLaneSets() != null && !processDefinition.getLaneSets().isEmpty()) {
                    laneSetArray = (new ObjectMapper()).createArrayNode();
                    Iterator var28 = processDefinition.getLaneSets().iterator();

                    while(var28.hasNext()) {
                        LaneSet laneSet = (LaneSet)var28.next();
                        ArrayNode laneArray = (new ObjectMapper()).createArrayNode();
                        if(laneSet.getLanes() != null && !laneSet.getLanes().isEmpty()) {
                            Iterator var14 = laneSet.getLanes().iterator();

                            while(var14.hasNext()) {
                                Lane lane = (Lane)var14.next();
                                ObjectNode laneJSON = (new ObjectMapper()).createObjectNode();
                                laneJSON.put("id", lane.getId());
                                if(StringUtils.isNotEmpty(lane.getName())) {
                                    laneJSON.put("name", lane.getName());
                                } else {
                                    laneJSON.put("name", "");
                                }

                                laneJSON.put("x", lane.getX());
                                laneJSON.put("y", lane.getY());
                                laneJSON.put("width", lane.getWidth());
                                laneJSON.put("height", lane.getHeight());
                                List<String> flowNodeIds = lane.getFlowNodeIds();
                                ArrayNode flowNodeIdsArray = (new ObjectMapper()).createArrayNode();
                                Iterator var19 = flowNodeIds.iterator();

                                while(var19.hasNext()) {
                                    String flowNodeId = (String)var19.next();
                                    flowNodeIdsArray.add(flowNodeId);
                                }

                                laneJSON.put("flowNodeIds", flowNodeIdsArray);
                                laneArray.add(laneJSON);
                            }
                        }

                        ObjectNode laneSetJSON = (new ObjectMapper()).createObjectNode();
                        laneSetJSON.put("id", laneSet.getId());
                        if(StringUtils.isNotEmpty(laneSet.getName())) {
                            laneSetJSON.put("name", laneSet.getName());
                        } else {
                            laneSetJSON.put("name", "");
                        }

                        laneSetJSON.put("lanes", laneArray);
                        laneSetArray.add(laneSetJSON);
                    }

                    if(laneSetArray.size() > 0) {
                        responseJSON.put("laneSets", laneSetArray);
                    }
                }

                laneSetArray = (new ObjectMapper()).createArrayNode();
                activityArray = (new ObjectMapper()).createArrayNode();
                var29 = processDefinition.getActivities().iterator();

                while(var29.hasNext()) {
                    ActivityImpl activity = (ActivityImpl)var29.next();
                    this.getActivity(processInstanceId, activity, activityArray, laneSetArray, processInstance, highLightedFlows, subProcessInstanceMap);
                }

                responseJSON.put("activities", activityArray);
                responseJSON.put("sequenceFlows", laneSetArray);
                return responseJSON;
            }
        }
    }

    private List<String> getHighLightedFlows(String processInstanceId, ProcessDefinitionEntity processDefinition) {
        List<String> highLightedFlows = new ArrayList();
        List<HistoricActivityInstance> historicActivityInstances = ((HistoricActivityInstanceQuery)this.historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId).orderByHistoricActivityInstanceStartTime().asc()).list();
        List<String> historicActivityInstanceList = new ArrayList();
        Iterator var6 = historicActivityInstances.iterator();

        while(var6.hasNext()) {
            HistoricActivityInstance hai = (HistoricActivityInstance)var6.next();
            historicActivityInstanceList.add(hai.getActivityId());
        }

        List<String> highLightedActivities = this.runtimeService.getActiveActivityIds(processInstanceId);
        historicActivityInstanceList.addAll(highLightedActivities);
        Iterator var15 = processDefinition.getActivities().iterator();

        while(true) {
            ActivityImpl activity;
            int index;
            do {
                do {
                    if(!var15.hasNext()) {
                        return highLightedFlows;
                    }

                    activity = (ActivityImpl)var15.next();
                    index = historicActivityInstanceList.indexOf(activity.getId());
                } while(index < 0);
            } while(index + 1 >= historicActivityInstanceList.size());

            List<PvmTransition> pvmTransitionList = activity.getOutgoingTransitions();
            Iterator var11 = pvmTransitionList.iterator();

            while(var11.hasNext()) {
                PvmTransition pvmTransition = (PvmTransition)var11.next();
                String destinationFlowId = pvmTransition.getDestination().getId();
                if(destinationFlowId.equals(historicActivityInstanceList.get(index + 1))) {
                    highLightedFlows.add(pvmTransition.getId());
                }
            }
        }
    }

    private void getActivity(String processInstanceId, ActivityImpl activity, ArrayNode activityArray, ArrayNode sequenceFlowArray, ProcessInstance processInstance, List<String> highLightedFlows, Map<String, ObjectNode> subProcessInstanceMap) {
        ObjectNode activityJSON = (new ObjectMapper()).createObjectNode();
        String multiInstance = (String)activity.getProperty("multiInstance");
        if(multiInstance != null && !"sequential".equals(multiInstance)) {
            multiInstance = "parallel";
        }

        ActivityBehavior activityBehavior = activity.getActivityBehavior();
        Boolean collapsed = Boolean.valueOf(activityBehavior instanceof CallActivityBehavior);
        Boolean expanded = (Boolean)activity.getProperty("isExpanded");
        if(expanded != null) {
            collapsed = Boolean.valueOf(!expanded.booleanValue());
        }

        Boolean isInterrupting = null;
        if(activityBehavior instanceof BoundaryEventActivityBehavior) {
            isInterrupting = Boolean.valueOf(((BoundaryEventActivityBehavior)activityBehavior).isInterrupting());
        }

        Iterator var14 = activity.getOutgoingTransitions().iterator();

        ArrayNode errorEventDefinitionsArray;
        while(var14.hasNext()) {
            PvmTransition sequenceFlow = (PvmTransition)var14.next();
            String flowName = (String)sequenceFlow.getProperty("name");
            boolean isHighLighted = highLightedFlows.contains(sequenceFlow.getId());
            boolean isConditional = sequenceFlow.getProperty("condition") != null && !((String)activity.getProperty("type")).toLowerCase().contains("gateway");
            boolean isDefault = sequenceFlow.getId().equals(activity.getProperty("default")) && ((String)activity.getProperty("type")).toLowerCase().contains("gateway");
            List<Integer> waypoints = ((TransitionImpl)sequenceFlow).getWaypoints();
            errorEventDefinitionsArray = (new ObjectMapper()).createArrayNode();
            ArrayNode yPointArray = (new ObjectMapper()).createArrayNode();

            for(int i = 0; i < waypoints.size(); i += 2) {
                errorEventDefinitionsArray.add((Integer)waypoints.get(i));
                yPointArray.add((Integer)waypoints.get(i + 1));
            }

            ObjectNode flowJSON = (new ObjectMapper()).createObjectNode();
            flowJSON.put("id", sequenceFlow.getId());
            flowJSON.put("name", flowName);
            flowJSON.put("flow", "(" + sequenceFlow.getSource().getId() + ")--" + sequenceFlow.getId() + "-->(" + sequenceFlow.getDestination().getId() + ")");
            if(isConditional) {
                flowJSON.put("isConditional", isConditional);
            }

            if(isDefault) {
                flowJSON.put("isDefault", isDefault);
            }

            if(isHighLighted) {
                flowJSON.put("isHighLighted", isHighLighted);
            }

            flowJSON.put("xPointArray", errorEventDefinitionsArray);
            flowJSON.put("yPointArray", yPointArray);
            sequenceFlowArray.add(flowJSON);
        }

        ArrayNode nestedActivityArray = (new ObjectMapper()).createArrayNode();
        Iterator var26 = activity.getActivities().iterator();

        while(var26.hasNext()) {
            ActivityImpl nestedActivity = (ActivityImpl)var26.next();
            nestedActivityArray.add(nestedActivity.getId());
        }

        Map<String, Object> properties = activity.getProperties();
        ObjectNode propertiesJSON = (new ObjectMapper()).createObjectNode();
        Iterator var30 = properties.keySet().iterator();

        while(true) {
            while(var30.hasNext()) {
                String key = (String)var30.next();
                Object prop = properties.get(key);
                if(prop instanceof String) {
                    propertiesJSON.put(key, (String)properties.get(key));
                } else if(prop instanceof Integer) {
                    propertiesJSON.put(key, (Integer)properties.get(key));
                } else if(prop instanceof Boolean) {
                    propertiesJSON.put(key, (Boolean)properties.get(key));
                } else if("initial".equals(key)) {
                    ActivityImpl act = (ActivityImpl)properties.get(key);
                    propertiesJSON.put(key, act.getId());
                } else {
                    ObjectNode errorEventDefinitionJSON;
                    ArrayList errorEventDefinitions;
                    Iterator var43;
                    if("timerDeclarations".equals(key)) {
                        errorEventDefinitions = (ArrayList)properties.get(key);
                        errorEventDefinitionsArray = (new ObjectMapper()).createArrayNode();
                        if(errorEventDefinitions != null) {
                            var43 = errorEventDefinitions.iterator();

                            while(var43.hasNext()) {
                                TimerDeclarationImpl timerDeclaration = (TimerDeclarationImpl)var43.next();
                                errorEventDefinitionJSON = (new ObjectMapper()).createObjectNode();
                                errorEventDefinitionJSON.put("isExclusive", timerDeclaration.isExclusive());
                                if(timerDeclaration.getRepeat() != null) {
                                    errorEventDefinitionJSON.put("repeat", timerDeclaration.getRepeat());
                                }

                                errorEventDefinitionJSON.put("retries", String.valueOf(timerDeclaration.getRetries()));
                                errorEventDefinitionJSON.put("type", timerDeclaration.getJobHandlerType());
                                errorEventDefinitionJSON.put("configuration", timerDeclaration.getJobHandlerConfiguration());
                                errorEventDefinitionsArray.add(errorEventDefinitionJSON);
                            }
                        }

                        if(errorEventDefinitionsArray.size() > 0) {
                            propertiesJSON.put(key, errorEventDefinitionsArray);
                        }
                    } else if("eventDefinitions".equals(key)) {
                        errorEventDefinitions = (ArrayList)properties.get(key);
                        errorEventDefinitionsArray = (new ObjectMapper()).createArrayNode();
                        if(errorEventDefinitions != null) {
                            var43 = errorEventDefinitions.iterator();

                            while(var43.hasNext()) {
                                EventSubscriptionDeclaration eventDefinition = (EventSubscriptionDeclaration)var43.next();
                                errorEventDefinitionJSON = (new ObjectMapper()).createObjectNode();
                                if(eventDefinition.getActivityId() != null) {
                                    errorEventDefinitionJSON.put("activityId", eventDefinition.getActivityId());
                                }

                                errorEventDefinitionJSON.put("eventName", eventDefinition.getEventName());
                                errorEventDefinitionJSON.put("eventType", eventDefinition.getEventType());
                                errorEventDefinitionJSON.put("isAsync", eventDefinition.isAsync());
                                errorEventDefinitionJSON.put("isStartEvent", eventDefinition.isStartEvent());
                                errorEventDefinitionsArray.add(errorEventDefinitionJSON);
                            }
                        }

                        if(errorEventDefinitionsArray.size() > 0) {
                            propertiesJSON.put(key, errorEventDefinitionsArray);
                        }
                    } else if("errorEventDefinitions".equals(key)) {
                        errorEventDefinitions = (ArrayList)properties.get(key);
                        errorEventDefinitionsArray = (new ObjectMapper()).createArrayNode();
                        if(errorEventDefinitions != null) {
                            var43 = errorEventDefinitions.iterator();

                            while(var43.hasNext()) {
                                ErrorEventDefinition errorEventDefinition = (ErrorEventDefinition)var43.next();
                                errorEventDefinitionJSON = (new ObjectMapper()).createObjectNode();
                                if(errorEventDefinition.getErrorCode() != null) {
                                    errorEventDefinitionJSON.put("errorCode", errorEventDefinition.getErrorCode());
                                } else {
                                    errorEventDefinitionJSON.putNull("errorCode");
                                }

                                errorEventDefinitionJSON.put("handlerActivityId", errorEventDefinition.getHandlerActivityId());
                                errorEventDefinitionsArray.add(errorEventDefinitionJSON);
                            }
                        }

                        if(errorEventDefinitionsArray.size() > 0) {
                            propertiesJSON.put(key, errorEventDefinitionsArray);
                        }
                    }
                }
            }

            if("callActivity".equals(properties.get("type"))) {
                CallActivityBehavior callActivityBehavior = null;
                if(activityBehavior instanceof CallActivityBehavior) {
                    callActivityBehavior = (CallActivityBehavior)activityBehavior;
                }

                if(callActivityBehavior != null) {
                    propertiesJSON.put("processDefinitonKey", callActivityBehavior.getProcessDefinitonKey());
                    ArrayNode processInstanceArray = (new ObjectMapper()).createArrayNode();
                    if(processInstance != null) {
                        List<Execution> executionList = this.runtimeService.createExecutionQuery().processInstanceId(processInstanceId).activityId(activity.getId()).list();
                        if(!executionList.isEmpty()) {
                            Iterator var40 = executionList.iterator();

                            while(var40.hasNext()) {
                                Execution execution = (Execution)var40.next();
                                ObjectNode processInstanceJSON = (ObjectNode)subProcessInstanceMap.get(execution.getId());
                                processInstanceArray.add(processInstanceJSON);
                            }
                        }
                    }

                    if(processInstanceArray.size() == 0 && StringUtils.isNotEmpty(callActivityBehavior.getProcessDefinitonKey())) {
                        ProcessDefinition lastProcessDefinition = (ProcessDefinition)this.repositoryService.createProcessDefinitionQuery().processDefinitionKey(callActivityBehavior.getProcessDefinitonKey()).latestVersion().singleResult();
                        if(lastProcessDefinition != null) {
                            ObjectNode processInstanceJSON = (new ObjectMapper()).createObjectNode();
                            processInstanceJSON.put("processDefinitionId", lastProcessDefinition.getId());
                            processInstanceJSON.put("processDefinitionKey", lastProcessDefinition.getKey());
                            processInstanceJSON.put("processDefinitionName", lastProcessDefinition.getName());
                            processInstanceArray.add(processInstanceJSON);
                        }
                    }

                    if(processInstanceArray.size() > 0) {
                        propertiesJSON.put("processDefinitons", processInstanceArray);
                    }
                }
            }

            activityJSON.put("activityId", activity.getId());
            activityJSON.put("properties", propertiesJSON);
            if(multiInstance != null) {
                activityJSON.put("multiInstance", multiInstance);
            }

            if(collapsed.booleanValue()) {
                activityJSON.put("collapsed", collapsed);
            }

            if(nestedActivityArray.size() > 0) {
                activityJSON.put("nestedActivities", nestedActivityArray);
            }

            if(isInterrupting != null) {
                activityJSON.put("isInterrupting", isInterrupting);
            }

            activityJSON.put("x", activity.getX());
            activityJSON.put("y", activity.getY());
            activityJSON.put("width", activity.getWidth());
            activityJSON.put("height", activity.getHeight());
            activityArray.add(activityJSON);
            var30 = activity.getActivities().iterator();

            while(var30.hasNext()) {
                ActivityImpl nestedActivity = (ActivityImpl)var30.next();
                this.getActivity(processInstanceId, nestedActivity, activityArray, sequenceFlowArray, processInstance, highLightedFlows, subProcessInstanceMap);
            }

            return;
        }
    }

    private JsonNode getProcessDefinitionResponse(ProcessDefinitionEntity processDefinition) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode pdrJSON = mapper.createObjectNode();
        pdrJSON.put("id", processDefinition.getId());
        pdrJSON.put("name", processDefinition.getName());
        pdrJSON.put("key", processDefinition.getKey());
        pdrJSON.put("version", processDefinition.getVersion());
        pdrJSON.put("deploymentId", processDefinition.getDeploymentId());
        pdrJSON.put("isGraphicNotationDefined", this.isGraphicNotationDefined(processDefinition));
        return pdrJSON;
    }

    private boolean isGraphicNotationDefined(ProcessDefinitionEntity processDefinition) {
        return ((ProcessDefinitionEntity)this.repositoryService.getProcessDefinition(processDefinition.getId())).isGraphicalNotationDefined();
    }
}
