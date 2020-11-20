const procmodelGenerator = new Blockly.Generator('PPE');

procmodelGenerator.PRECEDENCE = 0;

procmodelGenerator['artifact'] = function(block) {
  var dropdown_type = block.getFieldValue('Type');
  // TODO: Assemble PPEModel into code variable.
  var code = '...';
  // TODO: Change ORDER_NONE to the correct strength.
  return [code];
};