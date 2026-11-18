const spots = [
  [80, 200],
  [150, 140],
  [160, 250],
  [240, 190],
  [270, 130],
  [330, 230],
  [220, 270],
  [330, 150],
];
const pins = document.getElementById("pins"),
  log = document.getElementById("log"),
  hint = document.getElementById("hint");
let n = 0;
document.getElementById("checkin").addEventListener("click", () => {
  const [x, y] = spots[n++ % spots.length];
  const pin = document.createElementNS("http://www.w3.org/2000/svg", "g");
  pin.setAttribute("transform", "translate(" + x + " " + y + ")");
  pin.innerHTML =
    '<g class="drop"><path d="M0 0C-10-14-14-20-14-28a14 14 0 1 1 28 0C14-20 10-14 0 0Z" fill="#d9392b"/><circle cy="-28" r="5" fill="#fff"/></g>';
  pins.appendChild(pin);
  while (pins.children.length > 5) pins.firstChild.remove();
  const li = document.createElement("li");
  li.textContent = "Checkin às " + new Date().toLocaleTimeString("pt-BR");
  log.prepend(li);
  while (log.children.length > 3) log.lastChild.remove();
  hint.hidden = true;
});
