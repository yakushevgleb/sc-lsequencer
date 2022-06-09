
# SuperCollider L-System Sequencer

Parallel playing sequencer for [SuperCollider](https://github.com/supercollider/supercollider) based on Lindenmayer system

## Installation

 1. L-System string generator
 
	First of all you need to generate L-System string somewhere. There is a [NatureToolkit](https://quark.sccode.org/NatureToolkit/LSys/Help/LSys.html) quark for SuperCollider which can do that

 2. Classes installation
 
	 Run Platform.userExtensionDir in SuperCollider (MacOS) to see classes extensions directory. After that just clone that repo into that folder

 

## Usage
Generate L-System string

    var lsysTree = LSys("F1F1F1", [
      "0<0>0" -> "1",
      "0<0>1" -> "1[-F1F1]",
      "0<1>0" -> "1",
      "0<1>1" -> "1",
      "1<0>0" -> "0",
      "1<0>1" -> "1F1",
      "1<1>0" -> "1",
      "1<1>1" -> "0",
      "+" -> "-",
      "-" -> "+"
    ], "+-F[]");
    
    ~lsysTreeString = lsysTree.applyRules(22);

Create SynthDef

    SynthDef.new(\lpad, {
      arg buf=0, freq=1000, detune=0.2,
      amp=0.3, pan = 0.0, out = 0, rout = 0, rsend = (-20),
      atk = 0.1, dec = 0.1, sus = 1.0, rel = 1, curve = -4, gate = 1;
      var sig, env, detuneCtrl;
      env = EnvGen.ar(Env.adsr(atk, dec, sus, rel, curve: curve), gate,
        doneAction: 2
      );
    
      detuneCtrl = LFNoise1.kr(0.2!50).bipolar(detune).midiratio;
      sig = Osc.ar(buf, freq * detuneCtrl, {Rand(0,2pi)}!10);
    
      sig = Splay.ar(sig);
      sig = LeakDC.ar(sig);
      sig = Pan2.ar(sig, pan, amp);
      Out.ar(out, sig * env);
    }).add;

Create replacement functions

    ~baseFreq = 130.81;
    ~upperFreq = ~baseFreq * 1.12;
    ~lowerFreq = ~baseFreq / 1.5;
    
    ~funcF = {
      arg nodeItem, index, list;
      var dict = Dictionary[\freq -> ~baseFreq];
      if (list.at(0).isCollection, {
        switch(list.at(0).at(\id).asString,
          "UP", {
            dict.putPairs([
              \freq, (~upperFreq + 34)
            ]);
          },
          "DOWN", {
            dict.putPairs([
              \freq, (~lowerFreq - 15)
            ]);
          }, {
            dict.putPairs([
              \freq, (~baseFreq)
            ]);
          }
        );
      });
      dict;
    };
    
    ~func1 = {
      arg nodeItem, index, list;
      var dict = Dictionary[\freq -> (~baseFreq + 34)];
      switch(list.at(0).at(\id).asString,
        "UP", {
          dict.putPairs([
            \freq, (~upperFreq + 34)
          ]);
        },
        "DOWN", {
          dict.putPairs([
            \freq, (~lowerFreq - 25)
          ]);
        }, {
          dict.putPairs([
            \freq, (~baseFreq + 34)
          ]);
        }
      );
      dict;
    };
    
    ~func0 = {
      arg nodeItem, index, list;
      var dict = Dictionary[\freq -> (~baseFreq + 68)];
      switch(list.at(0).at(\id).asString,
        "UP", {
          dict.putPairs([
            \freq, (~upperFreq + 68)
          ]);
        },
        "DOWN", {
          dict.putPairs([
            \freq, (~lowerFreq - 35)
          ]);
        }, {
          dict.putPairs([
            \freq, (~baseFreq + 68)
          ]);
        }
      );
      dict;
    };
    
    ~funcPlus = {
      arg nodeItem, index, list;
      var dict = Dictionary.new;
      dict.putPairs([
        \freq, ~upperFreq,
        \id, "UP"
      ]);
      dict;
    };
    
    ~funcMinus = {
      arg nodeItem, index, list;
      var dict = Dictionary.new;
      dict.putPairs([
        \freq, ~lowerFreq,
        \id, "DOWN"
      ]);
      dict;
    };

Create L-System sequencer with specified offset between each pattern

    ~pads = LSequencer.new(
      lsysString: ~lsysTreeString,
      transformRules: [
        "F" -> ~funcF,
        "1" -> ~func1,
        "0" -> ~func0,
        "-" -> ~funcMinus,
        "+" -> ~funcPlus
      ],
      pbindKeys: Dictionary.newFrom([
        \instrument, \lpad,
        \dur, Pdefn(\dur, Prand([5.0, 6.0], inf)),
        \atk, Pdefn(\atk, 8),
        \dec, Pdefn(\dec, 0.1),
        \sus, Pdefn(\sus, 0.1),
        \rel, Pdefn(\rel, 4),
        \curve, Pdefn(\curve, 0.5),
        \detune, Pdefn(\detune, Pwhite(0.01, 0.8, inf)),
        \amp, Pdefn(\amp, 0.2),
        \out, 0,
        \buf, Pdefn(\buf, Pwhite([1.0, 9.0], inf))
      ]),
      offset: 1

Enjoy

    ~pads.play
