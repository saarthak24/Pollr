var pollIDs;
var polls = []
$.ajax({
    type: "POST",
    url: '/ppolls',
    complete: function(data) {
        if (JSON.parse(data.responseText) != null) {
            pollIDs = JSON.parse(data.responseText)
            console.log(pollIDs)
            for (var index = 0; index < pollIDs.length; index++) {
                $.ajax({
                    type: "POST",
                    url: '/ppollinfo',
                    async: false,
                    data: {
                        'poll_id': pollIDs[index]
                    },
                    complete: function(data) {
                        console.log(data)
                        $("#myDropdown").append('<a href="#" id=' + pollIDs[index] + '>' + JSON.parse(data.responseText).df.question + '</a>')
                        polls.push({
                            data: JSON.parse(data.responseText),
                            pollID: pollIDs[index]
                        })
                        console.log(index)
                    }
                });
            }
        }
        $("#chart").height('0em');
        console.log(polls)
    }
});

/* When the user clicks on the button,
toggle between hiding and showing the dropdown content */
function myFunction() {
    document.getElementById("myDropdown").classList.toggle("show");
}

// Close the dropdown menu if the user clicks outside of it
window.onclick = function(event) {
    if (!event.target.matches('.dropbtn')) {
        var dropdowns = document.getElementsByClassName("dropdown-content");
        var i;
        for (i = 0; i < dropdowns.length; i++) {
            var openDropdown = dropdowns[i];
            if (openDropdown.classList.contains('show')) {
                openDropdown.classList.remove('show');
            }
        }
        if (pollIDs.includes(event.target.id)) {
            pPopulate(event.target.id)
        }

    }
}

function pPopulate(pollID) {
    var pollData;
    var slider = 0;
    for (i = 0; i < polls.length; i++) {
        if (polls[i].pollID == pollID) {
            console.log(polls[i].data)
            pollData = polls[i].data
        }
    }
    for (i = 0; i < pollData.df.choices.length; i++) {
        if (pollData.df.choices[i] != null) {
            slider = 1;
        }
    }
    $("#pTitle").text("")
    $("#pType").text("")
    $("#chart").text("")
    $("#demographic").text("")
    $("#stats").text("")
    $("#pTitle").text(pollData.df.question)
    if(slider == 1)
        $("#pType").text("Slider")
    else
        $("#pType").text("Multiple Choice")
    $("#demographic").text(pollData.df.dem.charAt(0).toUpperCase() + pollData.df.dem.slice(1) + " - " + pollData.df.filter.charAt(0).toUpperCase() + pollData.df.filter.slice(1) + " | Voting District - " + pollData.df.voting_district)
    $("#stats").text(pollData.responses.length + " Responses")
    $("#chart").height('40em');
    AmCharts.makeChart("chart", {
        "type": "pie",
        "balloonText": "[[title]]<br><span style='font-size:14px'><b>[[value]]</b> ([[percents]]%)</span>",
        "titleField": "answer",
        "valueField": "length",
        "fontSize": 12,
        "theme": "default",
        "allLabels": [],
        "balloon": {},
        "titles": [],
        //"startDuration": 0,
        "dataProvider": [pollData.responses]
    });
}
