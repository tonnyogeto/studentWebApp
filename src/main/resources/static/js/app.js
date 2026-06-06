const api = {
	async request(url, options = {}) {
		const response = await fetch(url, {
			headers: {
				"Content-Type": "application/json",
				...(options.headers || {})
			},
			...options
		});

		const contentType = response.headers.get("content-type") || "";
		const isJson = contentType.includes("application/json");
		const payload = isJson ? await response.json() : await response.text();

		if (!response.ok) {
			const message = payload && payload.message ? payload.message : (typeof payload === "string" ? payload : "Request failed");
			throw new Error(message);
		}

		return payload;
	},

	json(method, url, body) {
		return this.request(url, {
			method,
			body: JSON.stringify(body)
		});
	},

	delete(url) {
		return this.request(url, { method: "DELETE" });
	}
};

function initSidebarShell() {
	const sidebarToggles = Array.from(document.querySelectorAll("[data-sidebar-toggle]"));
	if (sidebarToggles.length === 0) {
		return;
	}

	const storageKey = "ikonex-sidebar-collapsed";
	const body = document.body;
	const mobileQuery = window.matchMedia("(max-width: 1080px)");
	const savedState = localStorage.getItem(storageKey) === "true";

	const syncToggleState = () => {
		const collapsed = body.classList.contains("sidebar-collapsed");
		const open = body.classList.contains("sidebar-open");
		sidebarToggles.forEach(button => {
			button.setAttribute("aria-expanded", String(!(mobileQuery.matches ? open : collapsed)));
		});
	};

	const applyInitialState = () => {
		if (mobileQuery.matches) {
			body.classList.remove("sidebar-collapsed");
			body.classList.remove("sidebar-open");
		} else {
			body.classList.toggle("sidebar-collapsed", savedState);
			body.classList.remove("sidebar-open");
		}
		syncToggleState();
	};

	applyInitialState();

	sidebarToggles.forEach(button => {
		button.addEventListener("click", () => {
			if (mobileQuery.matches) {
				body.classList.toggle("sidebar-open");
				body.classList.remove("sidebar-collapsed");
				localStorage.setItem(storageKey, "false");
			} else {
				const nextCollapsed = !body.classList.contains("sidebar-collapsed");
				body.classList.toggle("sidebar-collapsed", nextCollapsed);
				body.classList.remove("sidebar-open");
				localStorage.setItem(storageKey, String(nextCollapsed));
			}
			syncToggleState();
		});
	});

	if (typeof mobileQuery.addEventListener === "function") {
		mobileQuery.addEventListener("change", applyInitialState);
	} else if (typeof mobileQuery.addListener === "function") {
		mobileQuery.addListener(applyInitialState);
	}

	const currentPath = window.location.pathname.replace(/\/$/, "") || "/";
	document.querySelectorAll(".sidebar-nav a").forEach(link => {
		const linkPath = link.getAttribute("href")?.replace(/\/$/, "") || "";
		if (linkPath === currentPath || (currentPath === "/" && linkPath === "/index.html")) {
			link.classList.add("active");
		}
	});
}

function escapeHtml(value) {
	return String(value ?? "")
		.replaceAll("&", "&amp;")
		.replaceAll("<", "&lt;")
		.replaceAll(">", "&gt;")
		.replaceAll('"', "&quot;");
}

function setMessage(element, message, isError = false) {
	if (!element) {
		return;
	}
	element.textContent = message || "";
	element.classList.toggle("error", Boolean(isError));
}

function fillSelect(select, items, placeholder, labeler) {
	if (!select) {
		return;
	}
	select.innerHTML = `<option value="">${escapeHtml(placeholder)}</option>`;
	for (const item of items) {
		const option = document.createElement("option");
		option.value = item.id;
		option.textContent = labeler(item);
		select.appendChild(option);
	}
}

async function loadStreams(select, placeholder = "Select class stream") {
	const streams = await api.request("/api/class-streams");
	fillSelect(select, streams, placeholder, stream => `${stream.streamCode} - ${stream.streamName}`);
	return streams;
}

async function loadSubjects(select, placeholder = "Select subject") {
	const subjects = await api.request("/api/subjects");
	fillSelect(select, subjects, placeholder, subject => `${subject.subjectCode} - ${subject.subjectName}`);
	return subjects;
}

function bindCrudForm({ form, endpoint, refresh, toPayload, onAfterSave }) {
	if (!form) {
		return;
	}
	const notice = form.querySelector(".notice");
	const resetButton = form.querySelector("[data-reset]");

	form.addEventListener("submit", async event => {
		event.preventDefault();
		const payload = toPayload(form);
		const id = form.dataset.editId;
		const method = id ? "PUT" : "POST";
		const url = id ? `${endpoint}/${id}` : endpoint;

		try {
			await api.json(method, url, payload);
			setMessage(notice, id ? "Record updated." : "Record created.");
			form.reset();
			delete form.dataset.editId;
			if (onAfterSave) {
				await onAfterSave();
			}
			await refresh();
		} catch (error) {
			setMessage(notice, error.message, true);
		}
	});

	if (resetButton) {
		resetButton.addEventListener("click", () => {
			form.reset();
			delete form.dataset.editId;
			setMessage(notice, "");
		});
	}
}

function rowActions(editHandler, deleteHandler) {
	return `
		<div class="row-actions">
			<button type="button" class="button" data-action="edit">Edit</button>
			<button type="button" class="button primary" data-action="delete">Delete</button>
		</div>
	`;
}

async function initStudentsPage() {
	const form = document.getElementById("studentForm");
	const tableBody = document.getElementById("studentsTableBody");
	const streamSelect = document.getElementById("studentClassStreamId");
	const refreshStreams = async () => loadStreams(streamSelect, "Select class stream");

	const render = async () => {
		const students = await api.request("/api/students");
		tableBody.innerHTML = students.map(student => `
			<tr>
				<td>${escapeHtml(student.admissionNumber)}</td>
				<td>${escapeHtml(student.firstName)} ${escapeHtml(student.lastName)}</td>
				<td>${escapeHtml(student.gender)}</td>
				<td>${escapeHtml(student.dateOfBirth || "")}</td>
				<td>${escapeHtml(student.classStreamCode || "")}</td>
				<td>${escapeHtml(student.classStreamName || "")}</td>
				<td>
					<div class="row-actions">
						<button type="button" class="button" data-edit="${student.id}">Edit</button>
						<button type="button" class="button primary" data-delete="${student.id}">Delete</button>
					</div>
				</td>
			</tr>
		`).join("");

		tableBody.querySelectorAll("[data-edit]").forEach(button => {
			button.addEventListener("click", () => {
				const student = students.find(item => String(item.id) === button.dataset.edit);
				if (!student) {
					return;
				}
				form.querySelector("[name='admissionNumber']").value = student.admissionNumber;
				form.querySelector("[name='firstName']").value = student.firstName;
				form.querySelector("[name='lastName']").value = student.lastName;
				form.querySelector("[name='gender']").value = student.gender;
				form.querySelector("[name='dateOfBirth']").value = student.dateOfBirth || "";
				form.querySelector("[name='classStreamId']").value = student.classStreamId;
				form.dataset.editId = student.id;
			});
		});

		tableBody.querySelectorAll("[data-delete]").forEach(button => {
			button.addEventListener("click", async () => {
				if (!confirm("Delete this student?")) {
					return;
				}
				await api.delete(`/api/students/${button.dataset.delete}`);
				await render();
			});
		});
	};

	bindCrudForm({
		form,
		endpoint: "/api/students",
		refresh: render,
		toPayload: currentForm => ({
			admissionNumber: currentForm.querySelector("[name='admissionNumber']").value,
			firstName: currentForm.querySelector("[name='firstName']").value,
			lastName: currentForm.querySelector("[name='lastName']").value,
			gender: currentForm.querySelector("[name='gender']").value,
			dateOfBirth: currentForm.querySelector("[name='dateOfBirth']").value || null,
			classStreamId: Number(currentForm.querySelector("[name='classStreamId']").value)
		}),
		onAfterSave: refreshStreams
	});

	await refreshStreams();
	await render();
}

async function initClassStreamsPage() {
	const form = document.getElementById("classStreamForm");
	const assignForm = document.getElementById("assignSubjectForm");
	const tableBody = document.getElementById("classStreamsTableBody");
	const assignStreamSelect = document.getElementById("assignStreamId");
	const assignSubjectSelect = document.getElementById("assignSubjectId");

	const refreshAssignments = async () => {
		const streams = await loadStreams(assignStreamSelect, "Select class stream");
		await loadSubjects(assignSubjectSelect, "Select subject");
	};

	const render = async () => {
		const streams = await api.request("/api/class-streams");
		tableBody.innerHTML = streams.map(stream => `
			<tr>
				<td>${escapeHtml(stream.streamCode)}</td>
				<td>${escapeHtml(stream.streamName)}</td>
				<td>${escapeHtml(stream.formLevel || "")}</td>
				<td><span class="tag">${stream.active ? "Active" : "Inactive"}</span></td>
				<td>${stream.studentCount}</td>
				<td>${stream.subjectCount}</td>
				<td>
					<div class="row-actions">
						<button type="button" class="button" data-edit="${stream.id}">Edit</button>
						<button type="button" class="button primary" data-delete="${stream.id}">Delete</button>
					</div>
				</td>
			</tr>
		`).join("");

		tableBody.querySelectorAll("[data-edit]").forEach(button => {
			button.addEventListener("click", () => {
				const stream = streams.find(item => String(item.id) === button.dataset.edit);
				if (!stream) {
					return;
				}
				form.querySelector("[name='streamCode']").value = stream.streamCode;
				form.querySelector("[name='streamName']").value = stream.streamName;
				form.querySelector("[name='formLevel']").value = stream.formLevel || "";
				form.querySelector("[name='active']").checked = stream.active;
				form.dataset.editId = stream.id;
			});
		});

		tableBody.querySelectorAll("[data-delete]").forEach(button => {
			button.addEventListener("click", async () => {
				if (!confirm("Delete this class stream?")) {
					return;
				}
				await api.delete(`/api/class-streams/${button.dataset.delete}`);
				await render();
				await refreshAssignments();
			});
		});
	};

	bindCrudForm({
		form,
		endpoint: "/api/class-streams",
		refresh: render,
		toPayload: currentForm => ({
			streamCode: currentForm.querySelector("[name='streamCode']").value,
			streamName: currentForm.querySelector("[name='streamName']").value,
			formLevel: currentForm.querySelector("[name='formLevel']").value,
			active: currentForm.querySelector("[name='active']").checked
		}),
		onAfterSave: refreshAssignments
	});

	assignForm?.addEventListener("submit", async event => {
		event.preventDefault();
		const notice = assignForm.querySelector(".notice");
		try {
			await api.json("POST", `/api/class-streams/${assignStreamSelect.value}/subjects/${assignSubjectSelect.value}`);
			setMessage(notice, "Subject assigned to class stream.");
			await render();
		} catch (error) {
			setMessage(notice, error.message, true);
		}
	});

	await refreshAssignments();
	await render();
}

async function initSubjectsPage() {
	const form = document.getElementById("subjectForm");
	const assignForm = document.getElementById("subjectAssignForm");
	const tableBody = document.getElementById("subjectsTableBody");
	const streamSelect = document.getElementById("subjectAssignStreamId");
	const subjectAssignSelect = document.getElementById("subjectAssignSubjectId");

	const refreshAssignments = async () => {
		await loadStreams(streamSelect, "Select class stream");
		await loadSubjects(subjectAssignSelect, "Select subject");
	};

	const render = async () => {
		const subjects = await api.request("/api/subjects");
		tableBody.innerHTML = subjects.map(subject => `
			<tr>
				<td>${escapeHtml(subject.subjectCode)}</td>
				<td>${escapeHtml(subject.subjectName)}</td>
				<td>${subject.classStreamCount}</td>
				<td>
					<div class="row-actions">
						<button type="button" class="button" data-edit="${subject.id}">Edit</button>
						<button type="button" class="button primary" data-delete="${subject.id}">Delete</button>
					</div>
				</td>
			</tr>
		`).join("");

		tableBody.querySelectorAll("[data-edit]").forEach(button => {
			button.addEventListener("click", () => {
				const subject = subjects.find(item => String(item.id) === button.dataset.edit);
				if (!subject) {
					return;
				}
				form.querySelector("[name='subjectCode']").value = subject.subjectCode;
				form.querySelector("[name='subjectName']").value = subject.subjectName;
				form.dataset.editId = subject.id;
			});
		});

		tableBody.querySelectorAll("[data-delete]").forEach(button => {
			button.addEventListener("click", async () => {
				if (!confirm("Delete this subject?")) {
					return;
				}
				await api.delete(`/api/subjects/${button.dataset.delete}`);
				await render();
				await refreshAssignments();
			});
		});
	};

	bindCrudForm({
		form,
		endpoint: "/api/subjects",
		refresh: render,
		toPayload: currentForm => ({
			subjectCode: currentForm.querySelector("[name='subjectCode']").value,
			subjectName: currentForm.querySelector("[name='subjectName']").value
		}),
		onAfterSave: refreshAssignments
	});

	assignForm?.addEventListener("submit", async event => {
		event.preventDefault();
		const notice = assignForm.querySelector(".notice");
		try {
			await api.json("POST", `/api/class-streams/${streamSelect.value}/subjects/${subjectAssignSelect.value}`);
			setMessage(notice, "Subject linked to class stream.");
		} catch (error) {
			setMessage(notice, error.message, true);
		}
	});

	await refreshAssignments();
	await render();
}

async function initResultsPage() {
	const classSelect = document.getElementById("resultsClassStreamId");
	const studentSelect = document.getElementById("resultsStudentId");
	const termInput = document.getElementById("resultsTerm");
	const classTableBody = document.getElementById("classResultsTableBody");
	const studentTableBody = document.getElementById("studentResultsTableBody");
	const classSummary = document.getElementById("classResultsSummary");
	const studentSummary = document.getElementById("studentResultsSummary");
	const pdfClassLink = document.getElementById("downloadClassReport");
	const pdfStudentLink = document.getElementById("downloadStudentReport");

	await loadStreams(classSelect, "Select class stream");
	const students = await api.request("/api/students");
	fillSelect(studentSelect, students, "Select student", student => `${student.admissionNumber} - ${student.firstName} ${student.lastName}`);

	async function loadClassResults() {
		if (!classSelect.value) {
			classTableBody.innerHTML = "";
			setMessage(classSummary, "Choose a class stream to view results.");
			return;
		}
		const term = termInput.value || "Term 1";
		const response = await api.request(`/api/results/class-streams/${classSelect.value}?term=${encodeURIComponent(term)}`);
		setMessage(classSummary, `${response.classStreamCode} - ${response.classStreamName} | Term ${response.term} | Class average ${response.classAverage}`);
		pdfClassLink.href = `/api/reports/class-streams/${classSelect.value}?term=${encodeURIComponent(term)}`;
		classTableBody.innerHTML = response.studentResults.map(row => `
			<tr>
				<td>${row.rank}</td>
				<td>${escapeHtml(row.admissionNumber)}</td>
				<td>${escapeHtml(row.studentName)}</td>
				<td>${row.totalScore}</td>
				<td>${row.averageScore}</td>
				<td>${escapeHtml(row.grade)}</td>
			</tr>
		`).join("");
	}

	async function loadStudentResults() {
		if (!studentSelect.value) {
			studentTableBody.innerHTML = "";
			setMessage(studentSummary, "Choose a student to view the report card.");
			return;
		}
		const term = termInput.value || "Term 1";
		const response = await api.request(`/api/results/students/${studentSelect.value}?term=${encodeURIComponent(term)}`);
		setMessage(studentSummary, `${response.studentName} | ${response.classStreamName} | Grade ${response.grade} | Rank ${response.rank}`);
		pdfStudentLink.href = `/api/reports/students/${studentSelect.value}?term=${encodeURIComponent(term)}`;
		studentTableBody.innerHTML = response.subjectScores.map(score => `
			<tr>
				<td>${escapeHtml(score.subjectName)}</td>
				<td>${score.score}</td>
			</tr>
		`).join("");
	}

	document.getElementById("loadClassResults")?.addEventListener("click", async () => {
		try {
			await loadClassResults();
		} catch (error) {
			setMessage(classSummary, error.message, true);
		}
	});

	document.getElementById("loadStudentResults")?.addEventListener("click", async () => {
		try {
			await loadStudentResults();
		} catch (error) {
			setMessage(studentSummary, error.message, true);
		}
	});

	termInput?.addEventListener("change", async () => {
		if (classSelect.value) {
			await loadClassResults();
		}
		if (studentSelect.value) {
			await loadStudentResults();
		}
	});
}

async function initAssessmentPage() {
	const form = document.getElementById("assessmentForm");
	const studentSelect = document.getElementById("assessmentStudentId");
	const subjectSelect = document.getElementById("assessmentSubjectId");
	const historyStudentSelect = document.getElementById("assessmentHistoryStudentId");
	const historyTermInput = document.getElementById("assessmentHistoryTerm");
	const historyBody = document.getElementById("assessmentHistoryTableBody");
	const historySummary = document.getElementById("assessmentHistorySummary");
	const loadHistoryButton = document.getElementById("loadAssessmentHistory");
	const notice = form?.querySelector(".notice");

	const refreshOptions = async () => {
		const students = await api.request("/api/students");
		fillSelect(studentSelect, students, "Select student", student => `${student.admissionNumber} - ${student.firstName} ${student.lastName}`);
		fillSelect(historyStudentSelect, students, "Select student", student => `${student.admissionNumber} - ${student.firstName} ${student.lastName}`);
		await loadSubjects(subjectSelect, "Select subject");
	};

	const loadHistory = async () => {
		if (!historyStudentSelect.value) {
			historyBody.innerHTML = "";
			setMessage(historySummary, "Choose a student to view their assessment history.");
			return;
		}

		const term = historyTermInput.value || "Term 1";
		const studentName = historyStudentSelect.selectedOptions[0]?.textContent || "Student";
		const assessments = await api.request(`/api/assessments?studentId=${historyStudentSelect.value}&term=${encodeURIComponent(term)}`);

		setMessage(historySummary, `${studentName} | ${term} | ${assessments.length} record(s)`);
		historyBody.innerHTML = assessments.length === 0
			? `<tr><td colspan="3">No assessment records found.</td></tr>`
			: assessments.map(item => `
				<tr>
					<td>${escapeHtml(item.subjectName)}</td>
					<td>${escapeHtml(item.term)}</td>
					<td>${item.score}</td>
				</tr>
			`).join("");
	};

	form?.addEventListener("submit", async event => {
		event.preventDefault();
		const payload = {
			studentId: Number(studentSelect.value),
			subjectId: Number(subjectSelect.value),
			term: form.querySelector("#assessmentTerm").value,
			score: Number(form.querySelector("#assessmentScore").value)
		};

		try {
			await api.json("POST", "/api/assessments", payload);
			setMessage(notice, "Assessment recorded.");
			historyStudentSelect.value = studentSelect.value;
			historyTermInput.value = payload.term;
			await loadHistory();
			form.reset();
			form.querySelector("#assessmentTerm").value = "Term 1";
		} catch (error) {
			setMessage(notice, error.message, true);
		}
	});

	form?.querySelector("[data-reset]")?.addEventListener("click", () => {
		form.reset();
		form.querySelector("#assessmentTerm").value = "Term 1";
		setMessage(notice, "");
	});

	loadHistoryButton?.addEventListener("click", async () => {
		try {
			await loadHistory();
		} catch (error) {
			setMessage(historySummary, error.message, true);
		}
	});

	historyTermInput?.addEventListener("change", async () => {
		if (historyStudentSelect.value) {
			await loadHistory();
		}
	});

	historyStudentSelect?.addEventListener("change", loadHistory);

	await refreshOptions();
	await loadHistory();
}

async function initDashboardPage() {
	try {
		const [streams, students, subjects] = await Promise.all([
			api.request("/api/class-streams"),
			api.request("/api/students"),
			api.request("/api/subjects")
		]);
		document.getElementById("metricStreams").textContent = streams.length;
		document.getElementById("metricStudents").textContent = students.length;
		document.getElementById("metricSubjects").textContent = subjects.length;
	} catch (error) {
		console.error(error);
	}
}

document.addEventListener("DOMContentLoaded", async () => {
	initSidebarShell();
	const page = document.body.dataset.page;
	if (page === "students") {
		await initStudentsPage();
	}
	if (page === "class-streams") {
		await initClassStreamsPage();
	}
	if (page === "subjects") {
		await initSubjectsPage();
	}
	if (page === "results") {
		await initResultsPage();
	}
	if (page === "assessment") {
		await initAssessmentPage();
	}
	if (page === "dashboard") {
		await initDashboardPage();
	}
});
