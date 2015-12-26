'use strict';
function generateUUID() {
    var d = new Date().getTime();
    var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
	var r = (d + Math.random()*16)%16 | 0;
	d = Math.floor(d/16);
	return (c=='x' ? r : (r&0x3|0x8)).toString(16);
    });
    return uuid;
};
var console = {
    log: function(){
	for(var a in arguments){
	    java.lang.System.out.print(a>0?', ':'');
	    java.lang.System.out.print(arguments[a]);
	}
	java.lang.System.out.println();
    }
};

console.log('Producer', producer);

var fn = {}; // context
// Adopted from here: https://gist.github.com/bripkens/8597903
// Makes ES7 Promises polyfill work on Nashorn https://github.com/jakearchibald/es6-promise
// (Haven't verified how correct it is, use with care)
(function(context) {
    'use strict';
    
    var Timer = Java.type('java.util.Timer');
    var Phaser = Java.type('java.util.concurrent.Phaser');

    var timer = new Timer('jsEventLoop', false);
    var phaser = new Phaser();
    
    var onTaskFinished = function() {
	phaser.arriveAndDeregister();
    };
    
    context.setTimeout = function(fn, millis /* [, args...] */) {
	var args = [].slice.call(arguments, 2, arguments.length);
	
	var phase = phaser.register();
	var canceled = false;
	timer.schedule(function() {
	    if (canceled) {
		return;
	    }
	    
	    try {
		fn.apply(context, args);		
	    } catch (e) {
		print(e);
	    } finally {
		onTaskFinished();
	    }
	}, millis);
	
	return function() {
	    onTaskFinished();
	    canceled = true;
	};
    };
    
    context.clearTimeout = function(cancel) {
	cancel();
    };
    
    context.setInterval = function(fn, delay /* [, args...] */) {
	var args = [].slice.call(arguments, 2, arguments.length);
	
	var cancel = null;
	
	var loop = function() {
	    cancel = context.setTimeout(loop, delay);
	    fn.apply(context, args);
	};
	
	cancel = context.setTimeout(loop, delay);
	return function() {
	    cancel();
	};
    };
    
    context.clearInterval = function(cancel) {
	cancel();
    };
    
})(fn);

var customers = {
    Jack: {
	account: 'AX449XZ2'
    },
    Gabriel: {
	account: 'B43SZ22N'
    },
    Lucie: {
	account: 'BU22A21U'
    },
    Bob:{
	account: 'A932X22M'
    },
    Lena:{
	account: 'Z28XSDB'
    }
};


var Bank = function (iBank){
    var randomTimeout = function(){ return Math.random() * 1000; };
    var randomId  = function(){ return generateUUID(); };
    var randomCustomer = function(){
	var keys = Object.keys(customers),
	    key = keys[Math.floor(Math.random() * keys.length)];
	return {customer: key, account: customers[key]};
    };
    var randomAmount = function(){ return Math.random() * 10000 * 100; };
    var randomDate = function(){ return Math.random() * new Date().getTime(); };

    var perform = function(){
	iBank.operate({
	    id: randomId(),
	    from: randomCustomer(),
	    to: randomCustomer(),
	    date: randomDate(),
	    amount: randomAmount()/100
	});
	fn.setTimeout(perform, randomTimeout());
    };
    console.log('Scheduling first execution');
    fn.setTimeout(perform, 5000);
};

var IBank = function () {    
    console.log('IBank constructed');
    var operations = [ 'send', 'withdraw', 'receive' ];

    var getRandom = function (arr) {
        return arr[Math.floor(arr.length * Math.random())];
    };
    
    this.operate = function(obj) {
	this[getRandom(operations)](obj);
    };

    this.send = function (obj) {
	producer.sendBody("seda:bank.send", JSON.stringify(obj));
    };
    
    this.withdraw = function (obj) {
	producer.sendBody("seda:bank.withdraw", obj);
    };

    this.receive = function (obj) {
	producer.sendBody("seda:bank.receive", obj);
    };
    
};

new Bank(new IBank());
