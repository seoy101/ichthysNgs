/**
 * 
 */
var cnt=0;
function modaless(){
  var conf= new Object();
  var childModal=null;
  var job_name =$("#job_name").val();
  var cpu =$("#cpu").val();
  var mem =$("#mem").val();
  conf["job_name"]=job_name;
  conf["cpu"]=cpu;
  conf["mem"]=mem;
  document.getElementById("conf").setAttribute("value",JSON.stringify(conf));

  if (window.showModelessDialog) { // Internet Explorer
    childModal=showModelessDialog("/views/pipeline", window, "dialogWidth:1000px; dialogHeight:700px; center:true; help:no scroll:yes");
  } else {
    childModal=window.open("/views/pipeline", ""+(++cnt), "width=1000, height=700, alwaysRaised=yes, location=no, toolbar=no, menubar=no");
  }
}

