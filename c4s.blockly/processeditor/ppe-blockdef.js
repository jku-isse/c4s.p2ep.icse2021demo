Blockly.Blocks['artifact'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("Artifact Type")
        .appendField(new Blockly.FieldDropdown([["JiraIssue","JiraIssue"], ["JamaItem","JamaItem"], ["ResourceLink","ResourceLink"], ["PullRequest","PullRequest"]]), "Type");
    this.setOutput(true, null);
    this.setColour(45);
 this.setTooltip("Defines what type of artifact is involved");
 this.setHelpUrl("");
  }
};

Blockly.Blocks['step'] = {
  init: function() {
    this.appendDummyInput()
        .appendField(new Blockly.FieldTextInput("StepId"), "StepId");
    this.appendStatementInput("Input")
        .setCheck("artuse")
        .appendField("Input");
    this.appendStatementInput("Transitions")
        .setCheck("transition");
    this.appendStatementInput("Output")
        .setCheck(["artuse", "qacheck"])
        .appendField("Output");
    this.setInputsInline(false);
    this.setPreviousStatement(true, "decisionnode");
    this.setNextStatement(true, "decisionnode");
    this.setColour(230);
 this.setTooltip("Steps");
 this.setHelpUrl("");
  }
};

Blockly.Blocks['noopstep'] = {
  init: function() {
    this.appendDummyInput()
        .appendField("NoOpStep");
    this.setInputsInline(false);
    this.setPreviousStatement(true, "decisionnode");
    this.setNextStatement(true, "decisionnode");
    this.setColour(230);
 this.setTooltip("Steps");
 this.setHelpUrl("");
  }
};

Blockly.Blocks['artuse'] = {
  init: function() {
    this.appendValueInput("NAME")
        .setCheck(null)
        .appendField(new Blockly.FieldLabelSerializable("Role"), "roletext")
        .appendField(new Blockly.FieldTextInput("defaultRole"), "NAME");
    this.setInputsInline(false);
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(30);
 this.setTooltip("");
 this.setHelpUrl("");
  }
};

Blockly.Blocks['comment'] = {
  init: function() {
    this.appendDummyInput()
        .appendField(new Blockly.FieldTextInput("% "), "comment");
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(180);
 this.setTooltip("");
 this.setHelpUrl("");
  }
};

Blockly.Blocks['fetchartifact'] = {
  init: function() {
    this.appendValueInput("Type")
        .setCheck("ArtifactType")
        .appendField(new Blockly.FieldLabelSerializable("Type"), "NAME");
    this.appendValueInput("IdAtOrigin")
        .setCheck("String")
        .appendField(new Blockly.FieldTextInput("IdType"), "IdType");
    this.setInputsInline(false);
    this.setOutput(true, "artifact");
    this.setColour(230);
 this.setTooltip("");
 this.setHelpUrl("");
  }
};

Blockly.Blocks['fieldaccessor'] = {
  init: function() {
    this.appendValueInput("var")
        .setCheck("artifacttype");
    this.appendDummyInput()
        .appendField("value of")
        .appendField(new Blockly.FieldTextInput("fieldName"), "NAME");
    this.setOutput(true, null);
    this.setColour(45);
 this.setTooltip("");
 this.setHelpUrl("");
  }
};

Blockly.Blocks['transition'] = {
  init: function() {
    this.appendValueInput("condition")
        .setCheck("Boolean")
        .appendField(new Blockly.FieldDropdown([["Enabled","ENABLED"], ["Active","ACTIVE"], ["Completed","COMPLETED"], ["Ignored","IGNORED"]]), "State")
        .appendField("Upon/If");
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(230);
 this.setTooltip("");
 this.setHelpUrl("");
  }
};

Blockly.Blocks['checkpoint'] = {
  init: function() {
    this.appendValueInput("NAME")
        .setCheck("Boolean")
        .appendField("wait until:");
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(230);
 this.setTooltip("");
 this.setHelpUrl("");
  }
};

Blockly.Blocks['streambooleanop'] = {
  init: function() {
    this.appendValueInput("NAME")
        .setCheck("Boolean")
        .appendField(new Blockly.FieldDropdown([["none match","NONEMATCH"], ["exactly one match","ONEMATCH"], ["at least one match","MOREMATCH"], ["all match","ALLMATCH"]]), "operator");
    this.setInputsInline(false);
    this.setPreviousStatement(true, null);
    this.setColour(230);
 this.setTooltip("");
 this.setHelpUrl("");
  }
};

Blockly.Blocks['stream'] = {
  init: function() {
    this.appendValueInput("NAME")
        .setCheck(null)
        .appendField("with each")
        .appendField(new Blockly.FieldVariable("el"), "NAME")
        .appendField("from");
    this.appendStatementInput("do")
        .setCheck(["streambooleanop", "streamnumberop", "streamtransformop", "streamcollectop"])
        .appendField("do");
    this.setOutput(true, null);
    this.setColour(230);
 this.setTooltip("");
 this.setHelpUrl("");
  }
};

Blockly.Blocks['streamnumberop'] = {
  init: function() {
    this.appendDummyInput()
        .appendField(new Blockly.FieldDropdown([["count","COUNT"], ["min","MIN"], ["max","MAX"]]), "operator");
    this.setInputsInline(false);
    this.setPreviousStatement(true, null);
    this.setColour(230);
 this.setTooltip("");
 this.setHelpUrl("");
  }
};

Blockly.Blocks['streamtransformop'] = {
  init: function() {
    this.appendValueInput("NAME")
        .setCheck(null)
        .appendField(new Blockly.FieldDropdown([["map","MAP"], ["flat map","FLATMAP"]]), "operator");
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(230);
 this.setTooltip("");
 this.setHelpUrl("");
  }
};

Blockly.Blocks['streamcollectop'] = {
  init: function() {
    this.appendDummyInput()
        .appendField(new Blockly.FieldDropdown([["to List","LIST"], ["to Set","SET"]]), "operator");
    this.setPreviousStatement(true, null);
    this.setColour(230);
 this.setTooltip("");
 this.setHelpUrl("");
  }
};

Blockly.Blocks['streamfilterop'] = {
  init: function() {
    this.appendValueInput("NAME")
        .setCheck("Boolean")
        .appendField("filter");
    this.setInputsInline(false);
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(230);
 this.setTooltip("");
 this.setHelpUrl("");
  }
};

Blockly.Blocks['qacheck'] = {
  init: function() {
    this.appendValueInput("constraint")
        .setCheck("Boolean")
        .appendField("QA Constraint");
    this.appendDummyInput()
        .appendField(new Blockly.FieldCheckbox("TRUE"), "isHardConstraint")
        .appendField("Must fulfill");
    this.setInputsInline(false);
    this.setPreviousStatement(true, null);
    this.setNextStatement(true, null);
    this.setColour(65);
 this.setTooltip("");
 this.setHelpUrl("");
  }
};