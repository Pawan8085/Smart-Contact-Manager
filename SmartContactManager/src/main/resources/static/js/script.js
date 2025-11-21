console.log("let's write javascript!")
const toggleSideBar = () =>{

    if($('.sidebar').is(":visible")){
        // hide the sidebar
        $(".sidebar").css("display", "none");
        $(".content").css("margin-left", "0%");
    }else{
        // show the sidebar
        $(".sidebar").css("display", "block");
        $(".content").css("margin-left", "20%");
    }
};

const search = () =>{

    let query = $("#search-input").val();
    
    if(query == ""){
        $(".search-result").hide();
    }else{
        // sending request to server 
        let url =  `http://localhost:8080/search/${query}`;

        fetch(url)
            .then((response) =>{
                 return response.json();
            }).then((data) => {
                // empty previous record
                $(".search-result").empty();
                if(data.length == 0){
                    $(".search-result").hide();
                }
                data.forEach(contact => {
                    let ele = $(`<a href='/user/contact/${contact.cid}'></a>`).text(contact.name);

                    ele.css({
                        'text-decoration': 'none',
                        'color': 'gray'
                        
                    });
                    
                    $(".search-result").append(ele, $("<br>"));
                });
            });
        $(".search-result").show();
    }
}


