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


// Razorpay
// first request to server to create order

const paymentStart = () =>{
    
    let amount = $("#payment_field").val();
   
    if(amount == "" || amount == null){
        swal("Amount is required !", "Please enter the amount", "warning");
        return;
    }

    // we will use ajax to send request to server to create order
    $.ajax({
        url: '/user/create_order',
        data: JSON.stringify({ amount: amount, info : 'order_request' }), // Convert data to JSON format
        contentType: 'application/json',
        type: 'POST',
        dataType: 'json',
        success: function(response) {
            // invoked when success
            console.log(response);
            if(response.status == 'created'){
                // open payment form here
                let options = {
                    key:'rzp_test_e8Og8Py8sM1LbB',
                    amount:response.amount,
                    currencey:'INR',
                    name:'Smart Contact Manager',
                    description:'Donation',
                    image:'https://avatars.githubusercontent.com/u/101393436?v=4',
                    order_id:response.id,
                    handler:function(response){
                        console.log(response.razorpay_payment_id);
                        console.log(response.razorpay_order_id);
                        console.log(response.razorpay_signature);
                        // console.log('payment successful');
                        
                        updatePaymentOnServer(response.razorpay_payment_id, response.razorpay_order_id, 'paid');
                       
                    },
                    prefill: {
                        "name": "",
                        "email": "",
                        "contact": ""
                    },
                    notes: {
                        "address": "Learn code with Pawan"
                        
                    },
                    theme: {
                        "color": "#3399cc"
                    }
                };

                let rzp = new Razorpay(options);
                rzp.on('payment.failed', function (response){
                    console.log(response.error.code);
                    console.log(response.error.description);
                    console.log(response.error.source);
                    console.log(response.error.step);
                    console.log(response.error.reason);
                    console.log(response.error.metadata.order_id);
                    console.log(response.error.metadata.payment_id);
                    // console.log("Oops payment failed!");
                   
                    swal("Oops payment failed !", "Please try again", "error");
                });
                rzp.open();
            }
        },
        error: function(error) {
            // invoked when error
            console.log(error);
            alert('Something went wrong !!');
        }
    });
    
};

function updatePaymentOnServer(payment_id, order_id, status)
{
    $.ajax({
        url: '/user/update_order',
        data: JSON.stringify({ payment_id: payment_id, order_id : order_id, status : status }), // Convert data to JSON format
        contentType: 'application/json',
        type: 'POST',
        dataType: 'json',
        success:function(response){
            swal("Your payment was successful !", "Thanks for your donation", "success");
        },
        error:function(error){
            swal("Payment Failed", "Your payment was processed, but an error occurred during the transaction. Please try again later.", "error");

        }
    })
} 