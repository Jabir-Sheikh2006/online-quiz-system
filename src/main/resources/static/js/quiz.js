const timer = document.getElementById("timer");
const timeText = document.getElementById("timeText");
const answerForm = document.getElementById("answerForm");
let remainingSeconds = secondsLeft;

function formatTime(totalSeconds) {
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;
    return `${minutes}:${String(seconds).padStart(2, "0")}`;
}

function updateTimer() {
    if (remainingSeconds <= 0) {
        timeText.textContent = "0:00";
        answerForm.submit();
        return;
    }

    timeText.textContent = formatTime(remainingSeconds);
    timer.classList.toggle("warning", remainingSeconds <= 60);
    remainingSeconds -= 1;
}

updateTimer();
setInterval(updateTimer, 1000);
