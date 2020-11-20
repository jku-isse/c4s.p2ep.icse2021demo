[{
  "type": "decisionnode",
  "message0": "work in parallel; start Steps: %1 %2 do %3 do %4 wait for  %5 (Synchonization Type)",
  "args0": [
    {
      "type": "field_dropdown",
      "name": "OutFlowType",
      "options": [
        [
          "CONT",
          "CONT"
        ],
        [
          "all at the same time (Synchronous)",
          "SYNC"
        ],
        [
          "once their input requirement is satisfied (Asynchronous)",
          "ASYNC"
        ]
      ]
    },
    {
      "type": "input_dummy"
    },
    {
      "type": "input_statement",
      "name": "Steps"
    },
    {
      "type": "field_dropdown",
      "name": "InFlowType",
      "options": [
        [
          "CONT",
          "CONT"
        ],
        [
          "all to complete (AND)",
          "AND"
        ],
        [
          "first to complete and then others (OR)",
          "OR"
        ],
        [
          "only one to complete (XOR)",
          "XOR"
        ]
      ]
    }
  ],
  "previousStatement": null,
  "nextStatement": null,
  "colour": 75,
  "tooltip": "Defines control flow",
  "helpUrl": ""
}]
