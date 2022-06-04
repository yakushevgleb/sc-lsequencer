LSequencer {
  var synth;
  *new {
    arg lsysString = String, transformRules = Dictionary.new, pbindKeys = Dictionary.new, offset = 0, ptparArgs = List.new;
    ^super.new.init(lsysString, transformRules, pbindKeys, offset, ptparArgs);
  }

  init {
    arg lsysString = String, transformRules = Dictionary.new, pbindKeys = Dictionary.new, offset = 0, ptparArgs = List.new;
    var replacedList = LReplacer.new(
      lsysString: lsysString,
      transformRules: transformRules
    );
    var pbindList = this.prListToPbind(list: replacedList, offset: offset, pbindKeys: pbindKeys);
    pbindList.postln;
    synth = Ptpar(pbindList, 1);
    ^synth;
  }

  prListToPbind {
    arg list, listIndex = 0, offset = 0, pbindKeys = Dictionary.new;
    var flatList = List.new;
    var baseList = List.new;
    list.do({
      arg item, index;
      if(item.isCollection && item.at(0) !== nil, {
        flatList.add(if(offset > 0, { offset * index }, { 1 * index }));
        flatList.add(
          ~listToPbindPads.value(
            list: item,
            listIndex: index,
            offset: offset,
            pbindKeys: pbindKeys
        ));
      }, {
        item.keys.do({
          arg key;
          var patternValue = item.at(key);
          var pbindValue = pbindKeys.at(key);
          if (pbindValue === nil, {
            pbindKeys.put(key, [patternValue]);
          }, {
            pbindKeys.put(key, pbindKeys.at(key) ++ [patternValue]);
          })
        });
      });
    });

    pbindKeys.keys.do({
      arg key;
      if(pbindKeys.at(key).isCollection, {
        Pdefn(key, Pseq(pbindKeys.at(key)));
        pbindKeys.put(key, Pdefn(key));
      })
    });
    if (listIndex > 0, {
      Pbind(*pbindKeys.asPairs);
    }, {
      flatList.addFirst(Pbind(*pbindKeys.asPairs));
      flatList.addFirst(0.0);
      ^flatList;
    });
  }
}