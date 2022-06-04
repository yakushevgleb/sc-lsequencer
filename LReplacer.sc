LReplacer {
  var replacedList;
  *new {
    arg lsysString = String, transformRules = Dictionary.new;
    ^super.new.init(lsysString, transformRules);
  }

  init {
    arg lsysString = String, transformRules = Dictionary.new;
    var splittedLsysList = this.splitString(lsysString);
    replacedList = this.prReplaceNodes(lsysList: splittedLsysList, nodesTransformDict: Dictionary.with(*transformRules));
    ^replacedList;
  }

  prTransformToList {
    arg lsysList;
    var transformedList = List.new, openBracketsCount = 0;

    var createSublist = {
      arg list, level;
      var innerLevel = level;
      if (innerLevel === 1, {
        list.add(List.new);
      }, {
        innerLevel = innerLevel - 1;
        createSublist.value(list.at(list.size - 1), innerLevel);
      })
    };

    var addToList = {
      arg list, level, node;
      var innerLevel = level;
      if (innerLevel === 0, {
        list.add(node);
      });
      if (innerLevel > 0, {
        innerLevel = innerLevel - 1;
        addToList.value(list: list.at(list.size - 1), level: innerLevel, node: node);
      });
    };

    lsysList.do({
      arg nodeItem, index;
      switch(nodeItem.asString,
        "[", {
          openBracketsCount = openBracketsCount + 1;
          createSublist.value(transformedList, openBracketsCount);
        },
        "]", {
          openBracketsCount = openBracketsCount - 1;
        },
        {
          addToList.value(list: transformedList, level: openBracketsCount, node: nodeItem);
        }
      );
    });

    ^transformedList;
  }

  prReplaceNodes {
    arg lsysList = [], nodesTransformDict = Dictionary.new;
    var transformedList = this.prTransformToList(lsysList);
    var replaceNodes = {
      arg list;
      list.do({
        arg item, index;
        if (item.isCollection, {
          replaceNodes.(item);
        }, {
          var dictItem = nodesTransformDict.at(item.asString);
          if (dictItem === nil, {
            list.put(index, item);
          }, {
            list.put(index, dictItem.(nodeItem: item, index: index, list: list));
          });
        });
      });
    };
    replaceNodes.(transformedList);
    ^transformedList;
  }

  splitString {
    arg lsysString = String;
    var splittedList = List.new;
    lsysString.do{
      arg item;
      splittedList.add(item);
    };
    ^splittedList;
  }

  getTransformedList {
    ^replacedList;
  }
}