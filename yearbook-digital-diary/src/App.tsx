import React, { useState, useRef, useEffect } from "react";
import {
  Pen,
  Eraser,
  Square,
  Circle,
  Image as ImageIcon,
  Type,
  Settings,
  ChevronRight,
  ChevronLeft,
  Undo,
  Redo,
  MoreHorizontal,
  SquareDashed,
  PlusCircle,
  Paperclip,
  Hand,
} from "lucide-react";

type DiaryEvent = {
  id: string;
  day: number;
  startSlot: number;
  endSlot: number;
  text: string;
  color: string;
};

const DAYS = ["MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"];
const DATES = ["4", "5", "6", "7", "8", "9", "10"];
const HOURS = Array.from({ length: 24 }, (_, i) => i + 1);
const SLOTS_PER_DAY = 48; // 24 hours * 2 (30 min blocks)
const SLOT_HEIGHT = 20;

const EVENT_THEMES = [
  {
    bg: "bg-yellow-400/90",
    border: "border-yellow-600",
    text: "text-yellow-950",
  },
  { bg: "bg-blue-300/90", border: "border-blue-500", text: "text-blue-950" },
  { bg: "bg-pink-300/90", border: "border-pink-500", text: "text-pink-950" },
  { bg: "bg-green-300/90", border: "border-green-500", text: "text-green-950" },
  {
    bg: "bg-purple-300/90",
    border: "border-purple-500",
    text: "text-purple-950",
  },
  {
    bg: "bg-orange-300/90",
    border: "border-orange-500",
    text: "text-orange-950",
  },
];

type Todo = {
  id: string;
  text: string;
  checked: boolean;
};

export default function App() {
  const [events, setEvents] = useState<DiaryEvent[]>([]);
  const [isDragging, setIsDragging] = useState(false);
  const [todos, setTodos] = useState<Todo[]>(
    Array(12)
      .fill(0)
      .map((_, i) => ({ id: i.toString(), text: "", checked: false })),
  );
  const [moods, setMoods] = useState<{ [day: number]: number }>({});

  const toggleTodo = (id: string) => {
    setTodos(
      todos.map((t) => (t.id === id ? { ...t, checked: !t.checked } : t)),
    );
  };

  const updateTodoText = (id: string, text: string) => {
    setTodos(todos.map((t) => (t.id === id ? { ...t, text } : t)));
  };

  const toggleMood = (dayIndex: number, moodIdx: number) => {
    setMoods((prev) => ({
      ...prev,
      [dayIndex]: prev[dayIndex] === moodIdx ? -1 : moodIdx,
    }));
  };
  const [dragStart, setDragStart] = useState<{
    day: number;
    slot: number;
  } | null>(null);
  const [dragCurrent, setDragCurrent] = useState<{
    day: number;
    slot: number;
  } | null>(null);
  const [currentColorIndex, setCurrentColorIndex] = useState(0);

  // Stop dragging on mouse up anywhere globally
  useEffect(() => {
    const handleMouseUp = () => {
      if (isDragging && dragStart && dragCurrent) {
        const start = Math.min(dragStart.slot, dragCurrent.slot);
        const end = Math.max(dragStart.slot, dragCurrent.slot);

        const newEvent = {
          id: Date.now().toString(),
          day: dragStart.day,
          startSlot: start,
          endSlot: end,
          text: "",
          color: (currentColorIndex % EVENT_THEMES.length).toString(),
        };

        setEvents((prev) => [...prev, newEvent]);
        setCurrentColorIndex((i) => i + 1);
      }
      setIsDragging(false);
      setDragStart(null);
      setDragCurrent(null);
    };

    window.addEventListener("mouseup", handleMouseUp);
    return () => window.removeEventListener("mouseup", handleMouseUp);
  }, [isDragging, dragStart, dragCurrent, currentColorIndex]);

  const handleMouseDown = (day: number, slot: number) => {
    setIsDragging(true);
    setDragStart({ day, slot });
    setDragCurrent({ day, slot });
  };

  const handleMouseEnter = (day: number, slot: number) => {
    if (isDragging && dragStart && dragStart.day === day) {
      setDragCurrent({ day, slot });
    }
  };

  const updateEventText = (id: string, text: string) => {
    setEvents(events.map((ev) => (ev.id === id ? { ...ev, text } : ev)));
  };

  const deleteEvent = (id: string) => {
    setEvents(events.filter((ev) => ev.id !== id));
  };

  return (
    <div className="min-h-screen bg-yellow-50 p-2 sm:p-8 flex items-start justify-center font-sans">
      {/* Main Notebook Container */}
      <div className="relative w-full max-w-[1400px] bg-white shadow-2xl rounded-2xl border-4 border-yellow-300 flex flex-col overflow-hidden select-none">
        {/* Top Tabs Area - Absolute positioned over the header */}
        <div className="absolute top-0 left-12 flex items-end h-[36px] z-10 gap-0.5">
          {["W18", "W19", "W20", "W21", "W22"].map((week, idx) => (
            <div
              key={week}
              className={`px-4 py-2 text-xs font-black rounded-t-lg border-2 border-b-0 border-yellow-500 shadow-sm
                ${idx === 0 ? "bg-yellow-100 text-yellow-900" : idx === 1 ? "bg-yellow-200 text-yellow-900" : idx === 2 ? "bg-yellow-300 text-yellow-900" : "bg-yellow-400 text-yellow-900"}
                ${idx === 0 ? "h-[38px] z-20" : "h-[34px] translate-y-[4px] hover:translate-y-0 transition-transform cursor-pointer z-10"}
              `}
            >
              {week}
            </div>
          ))}
        </div>

        {/* Toolbar - floating right */}
        <div className="absolute right-[-60px] lg:-right-16 top-16 w-12 bg-white rounded-xl shadow-lg border-2 border-yellow-200 flex flex-col items-center py-2 gap-3 transition-all hover:right-4 z-50">
          <button
            className="p-1.5 hover:bg-gray-100 rounded-md text-gray-700"
            title="Pen"
          >
            <Pen size={18} />
          </button>
          <button
            className="p-1.5 hover:bg-gray-100 rounded-md text-gray-700"
            title="Eraser"
          >
            <Eraser size={18} />
          </button>
          <button
            className="p-1.5 hover:bg-gray-100 rounded-md text-gray-700"
            title="Shapes"
          >
            <Square size={18} />
          </button>
          <button
            className="p-1.5 hover:bg-gray-100 rounded-md text-gray-700"
            title="Text"
          >
            <Type size={18} />
          </button>
          <button
            className="p-1.5 hover:bg-gray-100 rounded-md text-gray-700"
            title="Selection"
          >
            <SquareDashed size={18} />
          </button>
          <button
            className="p-1.5 hover:bg-gray-100 rounded-md text-gray-700"
            title="Insert"
          >
            <PlusCircle size={18} />
          </button>
          <button
            className="p-1.5 hover:bg-gray-100 rounded-md text-gray-700"
            title="Hand"
          >
            <Hand size={18} />
          </button>
          <div className="h-px w-8 bg-gray-200 my-1 font-sans"></div>
          <button
            className="p-1.5 hover:bg-gray-100 rounded-md text-gray-700"
            title="More"
          >
            <ChevronRight size={18} />
          </button>
        </div>

        {/* Top Header - Orange Bar */}
        <div className="bg-yellow-400 border-b-4 border-yellow-500 pt-[36px] pb-4 px-12 text-yellow-900 relative shadow-sm z-30">
          <div className="flex justify-end mb-2 mr-2 text-yellow-900 gap-2 absolute top-2 right-4">
            <button className="p-1 hover:bg-yellow-300 rounded font-bold">
              <Undo size={16} strokeWidth={2.5} />
            </button>
            <button className="p-1 hover:bg-yellow-300 rounded font-bold">
              <Redo size={16} strokeWidth={2.5} />
            </button>
            <button className="p-1 hover:bg-yellow-300 rounded font-bold">
              <MoreHorizontal size={16} strokeWidth={2.5} />
            </button>
          </div>

          <div className="flex">
            <div className="w-[124px]"></div>{" "}
            {/* Spacer for left sidebar (112px width + 12px margin) */}
            {DAYS.map((day, i) => (
              <div
                key={day}
                className="flex-1 text-center flex flex-col items-center"
              >
                <span className="text-2xl font-black tracking-tighter pt-2 opacity-90">
                  {day}
                </span>
                <span className="font-hand text-4xl mt-[-6px] font-bold opacity-80">
                  {DATES[i]}
                </span>
              </div>
            ))}
          </div>
        </div>

        {/* Notebook Body */}
        <div className="flex flex-1 relative bg-yellow-50/50 pt-3 pb-8 px-[12px] z-10">
          {/* Left Checkbox Sidebar */}
          <div className="w-[112px] mr-[12px] flex flex-col pt-3 border-r-2 border-yellow-200 bg-yellow-100/50 rounded-lg overflow-hidden relative">
            <div className="flex-1 overflow-y-auto px-2 pb-4 scrollbar-hide">
              <h3 className="text-[10px] font-black text-yellow-800 my-2 text-center tracking-widest uppercase border-b-2 border-yellow-300 pb-1">
                This Week
              </h3>
              <div className="flex flex-col gap-2">
                {todos.map((todo) => (
                  <div key={todo.id} className="flex gap-1.5 items-start">
                    <button
                      onClick={() => toggleTodo(todo.id)}
                      className={`mt-[2px] w-[14px] h-[14px] flex-shrink-0 border-2 rounded-[3px] transition-colors flex items-center justify-center
                        ${todo.checked ? "border-yellow-600 bg-yellow-400" : "border-yellow-400 bg-white"}
                      `}
                    >
                      {todo.checked && (
                        <svg
                          viewBox="0 0 14 14"
                          fill="none"
                          className="w-[10px] h-[10px] text-yellow-900"
                        >
                          <path
                            d="M3 7.5L5.5 10L11 4"
                            stroke="currentColor"
                            strokeWidth="2.5"
                            strokeLinecap="round"
                            strokeLinejoin="round"
                          />
                        </svg>
                      )}
                    </button>
                    <textarea
                      value={todo.text}
                      onChange={(e) => updateTodoText(todo.id, e.target.value)}
                      className={`flex-1 bg-transparent text-[11px] font-bold text-yellow-900 outline-none resize-none overflow-hidden leading-tight ${todo.checked ? "line-through opacity-50" : ""} placeholder-yellow-700/40`}
                      placeholder="Task..."
                      rows={2}
                    />
                  </div>
                ))}
              </div>
            </div>

            {/* Mini Calendar (May example) */}
            <div className="p-3 bg-yellow-200/50 border-t-2 border-yellow-300">
              <div className="text-[9px] font-black tracking-wider text-yellow-900 text-center mb-1.5 uppercase">
                May
              </div>
              <div className="grid grid-cols-7 gap-x-0.5 gap-y-1">
                {["M", "T", "W", "T", "F", "S", "S"].map((d, i) => (
                  <div
                    key={`mc-h-${i}`}
                    className="text-[7px] font-black text-yellow-700 text-center"
                  >
                    {d}
                  </div>
                ))}
                {/* 1st is Wed */}
                {Array(2)
                  .fill(null)
                  .map((_, i) => (
                    <div key={`empty-${i}`} />
                  ))}
                {Array.from({ length: 31 }).map((_, i) => {
                  const date = i + 1;
                  // Example active range: 4 to 10
                  const isActive = date >= 4 && date <= 10;
                  return (
                    <div
                      key={`d-${date}`}
                      className={`text-[8px] flex items-center justify-center w-[12px] h-[12px] mx-auto rounded-[3px] ${isActive ? "bg-yellow-500 font-black text-yellow-950" : "text-yellow-800 font-semibold"}`}
                    >
                      {date}
                    </div>
                  );
                })}
              </div>
            </div>
          </div>

          {/* Main Grid Area */}
          <div className="flex flex-1 border-2 border-yellow-300 bg-white relative rounded-lg shadow-inner overflow-hidden">
            {/* Render Grid columns per day */}
            {DAYS.map((day, dayIndex) => (
              <div
                key={day}
                className={`flex-1 flex flex-col relative border-r border-yellow-200`}
              >
                {Array.from({ length: SLOTS_PER_DAY }).map((_, slot) => {
                  const isHourRow = slot % 2 === 0;
                  const isDrafting =
                    isDragging &&
                    dragStart?.day === dayIndex &&
                    slot >= Math.min(dragStart.slot, dragCurrent!.slot) &&
                    slot <= Math.max(dragStart.slot, dragCurrent!.slot);
                  return (
                    <div
                      key={slot}
                      data-slot={slot}
                      onMouseDown={(e) => {
                        e.preventDefault();
                        handleMouseDown(dayIndex, slot);
                      }}
                      onMouseEnter={() => handleMouseEnter(dayIndex, slot)}
                      className={`
                          h-[${SLOT_HEIGHT}px] flex cursor-crosshair
                          border-b ${isHourRow && slot !== 0 ? "border-yellow-300" : "border-yellow-100/50"}
                          ${isDrafting ? "bg-yellow-400/50" : ""}
                        `}
                      style={{ height: `${SLOT_HEIGHT}px` }}
                    >
                      {/* Time Label */}
                      <div className="w-6 flex items-start justify-center border-r border-yellow-100/50 bg-yellow-50/30">
                        {isHourRow && (
                          <span className="text-[10px] text-yellow-700 font-bold leading-none mt-1">
                            {HOURS[slot / 2]}
                          </span>
                        )}
                      </div>
                      {/* The drawn sub-grid cells */}
                      <div className="flex-1 yb-grid-bg h-full"></div>
                    </div>
                  );
                })}

                {/* Render Finalized Events */}
                {events
                  .filter((ev) => ev.day === dayIndex)
                  .map((ev) => {
                    const top = ev.startSlot * SLOT_HEIGHT;
                    const height =
                      (ev.endSlot - ev.startSlot + 1) * SLOT_HEIGHT;
                    const themeIdx = parseInt(ev.color);
                    const theme =
                      EVENT_THEMES[
                        isNaN(themeIdx) ? 0 : themeIdx % EVENT_THEMES.length
                      ];
                    return (
                      <div
                        key={ev.id}
                        className={`absolute left-7 right-1 rounded shadow-sm border-2 ${theme.border} ${theme.bg} overflow-hidden flex z-20`}
                        style={{
                          top: `${top + 1}px`,
                          height: `${height - 2}px`,
                        }}
                      >
                        {/* Left colored accent bar */}
                        <div className="w-1.5 h-full bg-black/10"></div>
                        <div className="flex-1 p-1 relative group h-full overflow-hidden">
                          <textarea
                            value={ev.text}
                            onChange={(e) =>
                              updateEventText(ev.id, e.target.value)
                            }
                            className={`w-full h-full bg-transparent resize-none outline-none text-[11px] font-bold leading-tight ${theme.text} font-sans placeholder-black/30`}
                            placeholder="Add note..."
                            onPointerDownCapture={(e) => e.stopPropagation()} // Let user click without triggering drag
                          />
                          <button
                            onClick={(e) => {
                              e.stopPropagation();
                              deleteEvent(ev.id);
                            }}
                            className="absolute top-0.5 right-0.5 opacity-0 group-hover:opacity-100 bg-white/50 hover:bg-white rounded p-0.5 text-red-500 transition-opacity"
                          >
                            <svg
                              width="10"
                              height="10"
                              viewBox="0 0 24 24"
                              fill="none"
                              stroke="currentColor"
                              strokeWidth="2"
                            >
                              <path d="M18 6L6 18M6 6l12 12" />
                            </svg>
                          </button>
                        </div>
                      </div>
                    );
                  })}

                {/* Mood Selector at the bottom of the column */}
                <div className="flex justify-center gap-1.5 items-center py-2 bg-yellow-100/30 border-b-2 border-yellow-200 h-[36px] flex-shrink-0 mt-auto">
                  {[0, 1, 2, 3, 4].map((moodIdx) => {
                    const selected = moods[dayIndex] === moodIdx;
                    return (
                      <button
                        key={`mood-${dayIndex}-${moodIdx}`}
                        onClick={() => toggleMood(dayIndex, moodIdx)}
                        className={`w-[12px] h-[12px] rounded-full transition-all border
                           ${selected ? "bg-yellow-500 border-yellow-600 scale-[1.3]" : "bg-white border-yellow-400 hover:bg-yellow-200 opacity-70"}
                         `}
                        title={`Mood ${moodIdx + 1}`}
                      />
                    );
                  })}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
