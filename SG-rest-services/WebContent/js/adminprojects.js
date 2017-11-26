$(document).ready( function() {
	
	$("#btnConsumir").button().click(function(){
		testConsume();
	});
	
	$("#btnConsTP").button().click(function(){
		testConsumeText();
	});
	
	function testConsumeText() {

		var rootURL = "http://localhost:9999/ScrumGraph/sgrest/project/testingConsume2/";
	    $.ajax({
	        type: 'POST',
	        contentType: 'text/plain',
	        url: rootURL,
	        dataType: "json",
	        data:
	        	JSON.stringify({
	        		"id": 10, "tipo": "PROYECTO", "properties": {"propn": "valorn", "prop2": "valor2", "prop1": "valor1"} 	
	        	}),
	        	
	        success: function(data, textStatus, jqXHR){
	            alert('Servicio REST Consume success!');
	        },
	        error: function(jqXHR, textStatus, errorThrown){
	            alert('Consume error: ' + textStatus);
	        }
	    });
	
	}
	
	
	function testConsume() {
		var rootURL = "http://localhost:8181/ScrumGraph/sgrest/project/testingConsume/";
	    $.ajax({
	        type: 'POST',
	        contentType: 'application/json',
	        url: rootURL,
	        dataType: "json",
	        data:
	        	JSON.stringify({
	        		"id": 10, "tipo": "PROYECTO", "properties": {"propn": "valorn", "prop2": "valor2", "prop1": "valor1"} 	
	        	}),
	        	
	        success: function(data, textStatus, jqXHR){
	            alert('Servicio REST Consume success!');
	        },
	        error: function(jqXHR, textStatus, errorThrown){
	            alert('Consume error: ' + textStatus);
	        }
	    });
	}
	
	$("#btnBuscarProy").button().click(function(){
		
		var servicioRest = "http://localhost:8181/ScrumGraph/sgrest/project/buscar/"; 
		servicioRest = servicioRest.concat($("#txtID").val());
		
		$.ajax({ 
            type: "GET",
            url: servicioRest,
            success: function(data){
              $("#rta").html(data);
            }
        });		
	});	
	
	$("#btnCrearProy").button().click(function(){
				
		var servicioRest = "http://localhost:8181/ScrumGraph/sgrest/project/crear/"; 
		servicioRest = servicioRest.concat($("#txtNomProy").val());
		
		$.ajax({ 
            type: "POST",
            dataType: "json",
            url: servicioRest,
            success: function(data){
            	alert(data);
              $("#msjCrearProy").html(data);
            }
        });		
	});		
	
	
	$("#btnTesting").button().click(function(){
		
		var servicioRest = "http://localhost:8181/ScrumGraph/sgrest/TR/testing"; 
		$.ajax({ 
            type: "POST",
            url: servicioRest,
            success: function(data){
              $("#rta").html(data);
            }
        });		
	});	
});