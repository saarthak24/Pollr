$.ajax({
        type: "POST",
        url: '/ppolls',
        complete: function(data) {
            var pollIDs = JSON.parse(data.responseText)
            for (index = 0; index < pollIDs.length; ++index) {
                $("#pollSelect").append('<option value=' + pollIDs[index] + '>' + 'Test' + '</option>') //NEED TO FINISH
            }
            pPopulate($("#pollSelect").value)
        }
    });

$("#pollSelect").change(function () {
       pPopulate(this.value); //NEED TO FINISH
   });

function pPopulate(pollID){
    $.ajax({
            type: "GET",
            url: '/ppollinfo',
            complete: function(data) {
                console.log(data)
            }
        });
    //AJAX GET REQUEST TO GET STUFF
    $("#pTitle").val("")
    $("#pType").val("")
    $("#chart").val("")
    $("#demographic").val("")
    $("#stats").val("")
    // $("#pTitle").val()
    // $("#pType").val()
    // $("#chart").val()
    // $("#demographic").val()
    // $("#stats").val()
    //If type is slider --> Create bar graph
    //if type is multiple choice --> Create pie graph
    {
"type": "pie",
"balloonText": "[[title]]<br><span style='font-size:14px'><b>[[value]]</b> ([[percents]]%)</span>",
"titleField": "country",
"valueField": "litres",
"fontSize": 12,
"theme": "default",
"allLabels": [],
"balloon": {},
"titles": [],
"dataProvider": [
    {
        "country": "Czech Republic",
        "litres": "356.9"
    },
    {
        "country": "Ireland",
        "litres": 131.1
    },
    {
        "country": "Germany",
        "litres": 115.8
    },
    {
        "country": "Australia",
        "litres": 109.9
    },
    {
        "country": "Austria",
        "litres": 108.3
    },
    {
        "country": "UK",
        "litres": 65
    },
    {
        "country": "Belgium",
        "litres": "20"
    }
]
}
}
