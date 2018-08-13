// Adapted from CodeExplainedRepo


const cvs = document.getElementById("snake");
const ctx = cvs.getContext("2d");

// create the snake unite
const box = 32;

// load the background and food icons

const ground = new Image();
ground.src = "img/ground.png";

const foodImg = new Image();
foodImg.src = "img/food.png";

// load audio files

let dead = new Audio();
let eat = new Audio();
let up = new Audio();
let right = new Audio();
let left = new Audio();
let down = new Audio();

dead.src = "audio/dead.mp3";
eat.src = "audio/eat.mp3";
up.src = "audio/up.mp3";
right.src = "audio/right.mp3";
left.src = "audio/left.mp3";
down.src = "audio/down.mp3";

// create the snake

let snake = [];

snake[0] = {
    x : 9 * box,
    y : 10 * box
};

// create the food object
// Randomize the x, y coordinates for added unpredictabiliy

let food = {
    x : Math.floor(Math.random()*17+1) * box,
    y : Math.floor(Math.random()*15+3) * box
}

// create the score var

let score = 0;

// move the snake with the arrow keys.

let d;

document.addEventListener("keydown", direction);

function direction(event){
    let key = event.keyCode;
    if( key == 37 && d != "RIGHT"){
        left.play();
        d = "LEFT";
    }else if(key == 38 && d != "DOWN"){
        up.play();
        d = "UP";
        
    }else if(key == 39 && d != "LEFT"){
        right.play();
        d = "RIGHT";
        
    }else if(key == 40 && d != "UP"){
        down.play();
        d = "DOWN";
        
    }
}

// check if the snake runs into itself
function collision(head, array){
    for(let i = 0; i < array.length; i++){
        if(head.x == array[i].x && head.y == array[i].y){
            return true;
        }
    }
    return false;
}

// draw everything to the canvas

function draw(){
    
    // draw the ground image
    ctx.drawImage(ground,0,0);
    
    // draw the boxes for the snake
    for( let i = 0; i < snake.length ; i++){
        // the "head" of the snake is green; otherwise white
        ctx.fillStyle = ( i == 0 ) ? "green" : "white";
        ctx.fillRect(snake[i].x, snake[i].y, box, box);
        
        // create a red highlight over the box
        ctx.strokeStyle = "red";
        ctx.strokeRect(snake[i].x, snake[i].y, box, box);
    }
    
    ctx.drawImage(foodImg, food.x, food.y);
    
    // old head position
    let snakeX = snake[0].x;
    let snakeY = snake[0].y;
    
    // which direction
    if( d == "LEFT") snakeX -= box;
    if( d == "UP") snakeY -= box;
    if( d == "RIGHT") snakeX += box;
    if( d == "DOWN") snakeY += box;
    
    // if the snake eats the food
    if(snakeX == food.x && snakeY == food.y){
        eat.play();
        score++;
        // generate a new food object randomly
        food = {
            x : Math.floor(Math.random()*17+1) * box,
            y : Math.floor(Math.random()*15+3) * box
        }
    }else{ // otherwise...
           // remove the tail
        snake.pop();
    }
    
    // initialize new Head
    let newHead = {
        x : snakeX,
        y : snakeY
    }
    
    // if the snake hits the border or itself--game over
    if(snakeX < box || snakeX > 17 * box || snakeY < 3*box || snakeY > 17 * box || collision(newHead, snake)){
        clearInterval(game);
        dead.play();
    }
    
    // add newHead to the snake object
    snake.unshift(newHead);
    
    ctx.fillStyle = "white";
    ctx.font = "45px Changa one";
    ctx.fillText(score, 2*box, 1.6*box);
}

// call draw function every 100 ms

let game = setInterval(draw,100);