/**
 * 
*/
  var xmlhttp = function(){
    if(XMLHttpRequest) return new XMLHttpRequest();
    else if(ActiveXObject) return new ActiveXObject("Micrhsoft.XMLHTTP");
    else {alert("try it another browser"); return null}     
  }   
  function onClick(html){
    var element = document.getElementById(html);
    if($("#"+html).is(":checked")){
      var obj = new Object();
      window.json[html]=obj;
      console.log(window.json);  
    }
    else{
      delete window.json[html]
      console.log(window.json);
    }
  };   
  function getHtml(html){
    var http = xmlhttp();
    http.open("GET", "/views/"+html, true);
    http.onreadystatechange = function() {
      if (http.readyState==4) {
        if(http.status === 200 || http.status == 0)
        {          
          var fieldSet = http.responseText;
          window.modalSfw.addStep(++window.stepCnt ,fieldSet);
       }
        else{
          alert("xxxxxxxxx");
        }
      }
    }
    http.send(null);   
  }
  function getKeys(){
    var keys = Object.keys(window.json);
    for(var i=0; i< keys.length ;i++){
      getHtml(keys[i]);
    }   
  }
  function resetModal(){   
    location.reload(true);  
    console.log(json);
  }
  function htmlTojson(){
      for(var i=0; i< window.stepCnt; i++){
        var html=Object.keys(window.json)[i];
        $('fieldset#'+html+' input').serializeObject(html);
      }      
      postJson();
  }
  function setConf(){
      var job_name =$("#job_name").val();
      var cpu =$("#cpu").val();
      var mem =$("#mem").val();
      window.conf["job_name"]=job_name;
      window.conf["cpu"]=cpu;
      window.conf["mem"]=mem;
  }
  function postJson(){
    $.ajax({
      type:'POST',
      url: "http://localhost:9000/analysis",
      dataType:"json",
      data: {
             "conf": JSON.stringify(window.conf),
             "pipeline": JSON.stringify(window.json)
              },
      success: function(e){
        alert("ASdasd");
      }
    });
  }

$(document).ready(function () {

   window.json= new Object();
   window.conf= new Object();
   window.stepCnt=0;
   window.modalForm = $("#wizard_example2");    
//   window.modalForm.validate();
   window.modalSfw = $("#wizard_example2").stepFormWizard({  
     height: 'auto',
     finishBtn: $('<input class="finish-btn sf-right sf-btn" id="finish-button" type="submit" value="FINISH"/>'),  
     onNext: function() {
       var valid = window.modalForm.valid();
       window.modalSfw.refresh();
       return valid;
     },
     onFinish: function() {
       var valid = window.modalForm.valid();
       window.modalSfw.refresh();        
       console.log(valid); 
       htmlTojson();
      
       return valid;
     }
   });
   $("#finish-button").hide();
   
   $("#addStep-button").click(function(){
     if(window.stepCnt>0){
       $("#finish-button").hide();
       for(;window.sepCnt>0;--window.stepCnt){
         window.modalSfw.removeStep(window.stepCnt);
       }
     }
     getKeys();
     $("#finish-button").show();
   });
  $.fn.serializeObject = function(html) {
    try {
     var arr = this.serializeArray();
      if(arr){  
        jQuery.each(arr, function() {
          window.json[html][this.name] = this.value;
        });       
      }
    }catch(e) { 
      alert(e.message);
    }finally  {}
  };
});
$(window).load(function () {
    $(".sf-step").mCustomScrollbar({
        theme: "dark-3",
        scrollButtons: {
            enable: true
        }
    });
});
