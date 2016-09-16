/**
 * 
 */
  var stepCnt=0;
  var cnt=0;
  var modalSfw;
  var modalForm;
  var json= new Object();
  var conf;
  
  function onClick(html){
    var element = document.getElementById(html);
    if($("#"+html).is(":checked")){
      var obj = new Object();      
      json[html]=obj;
      cnt++;
      console.log(json);  
    }
    else{
      delete json[html]
      console.log(json);
    }
  };   
  function getHtml(html){
    $.ajax({
      url:'http://localhost:9000/views/'+html,
      type:'GET',
      async:'false',
      success: function(data) {
        modalSfw.addStep(json[html].order,$(data));
        alert("asdfasdf");
      }
   });   
  }
  function getKeys(){
    var keys = Object.keys(json);
    for(var i=0; i< keys.length ;i++){
      json[keys[i]].exe=keys[i];
      json[keys[i]].order=++stepCnt;
      json[keys[i]].deleted="no";
      getHtml(keys[i]);
    }   
  }
  function htmlTojson(){
      for(var i=0; i< window.stepCnt; i++){
        var html=Object.keys(json)[i];
        $('fieldset#'+html+' input').serializeObject(html);
      }      
      json.fileName=$("#file").val();
      alert(JSON.stringify(json));
      postJson();
  }
  function postJson(){
    $.ajax({
      type:'POST',
      url: "http://localhost:9000/analysis",
      dataType:"json",
      data: {
              "conf": conf,
             "pipeline": JSON.stringify(json)
              },
      success: function(e){
        alert("ASdasd");
      }
    });
  }
  
$(document).ready(function () {

   modalForm = $("#wizard_example2");    
   modalForm.validate();
   modalSfw = $("#wizard_example2").stepFormWizard({  
     height: 'auto',
     finishBtn: $('<input class="finish-btn sf-right sf-btn" id="finish-button" type="submit" value="FINISH"/>'),  
     onNext: function() {
       var valid = modalForm.valid();
       modalSfw.refresh();
       return valid;
     },
     onFinish: function() {
       var valid = modalForm.valid();
       modalSfw.refresh();        
       console.log(valid); 
       htmlTojson();
       window.blur();
       return valid;
     }
   });
   $("#finish-button").hide();
   
   $("#addStep-button").click(function(){
     if(cnt==0){
       alert("confirm one or more pipeline")
     }
     else{
       if(window.stepCnt>0){
         $("#finish-button").hide();
         for(;stepCnt>0;--stepCnt){
           modalSfw.removeStep(stepCnt);
         }
       }
       getKeys();
       modalSfw.next();
       $("#finish-button").show();
     }
   });
  $.fn.serializeObject = function(html) {
    try {
     var arr = this.serializeArray();
      if(arr){  
        jQuery.each(arr, function() {
          json[html][this.name] = this.value;
        });       
      }
    }catch(e) { 
      alert(e.message);
    }finally  {}
  };
  conf= window.opener.document.form1.conf.value;
});
$(window).load(function () {
    $(".sf-step").mCustomScrollbar({
        theme: "dark-3",
        scrollButtons: {
            enable: true
        }
    });    
});
