package com.bss.protobuff.dept.transformer;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bss.protobuf.dept.business.dto.Book;
import com.bss.protobuf.dept.business.dto.Department;
import com.bss.protobuf.dept.business.dto.Genre;
import com.bss.protobuf.dept.business.dto.Student;
import com.bss.protobuf.dept.business.dto.Subject;
import com.bss.protobuff.dept.DepartmentProto.Integer;
import com.bss.protobuff.dept.DepartmentProto.Integer.Builder;
import com.bss.protobuff.dept.DepartmentProto.PBook;
import com.bss.protobuff.dept.DepartmentProto.PDepartment;
import com.bss.protobuff.dept.DepartmentProto.PGenre;
import com.bss.protobuff.dept.DepartmentProto.PStudent;
import com.bss.protobuff.dept.DepartmentProto.PSubject;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * 
 * @author amit
 *
 */
public class DepartmentTransformer implements Transformer<Department> {
	private static final Logger LOGGER = LoggerFactory.getLogger(DepartmentTransformer.class);

	@Override
	public byte[] serialize(final Department department) {
		byte[] response = null;
		if (department != null) {
			List<Book> books = department.getBooks();
			List<Student> students = department.getStudents();
			PDepartment.Builder deptBuilder = PDepartment.newBuilder();
			
			deptBuilder.setDepartmentName(department.getDepartmentName());
			deptBuilder.setDepartmentTag(department.getDepartmentTag());
			Builder departmentId = getProtoInteger(department.getDepartmentId());
			deptBuilder.setDepartmentId(departmentId);

			processBooksBuilder(books, deptBuilder);
			processStudentsBuilder(students, deptBuilder);
			
			PDepartment pDept = deptBuilder.build();
			response = pDept.toByteArray();
		}

		return response;
	}

	@Override
	public Department deserialize(final byte[] stream) {
		Department department = null;
		try {
			if (stream != null && stream.length > 0) {
				PDepartment pDept;
				pDept = PDepartment.parseFrom(stream);
				department = new Department();
				department.setDepartmentId(pDept.getDepartmentId().getValue());
				department.setDepartmentName(pDept.getDepartmentName());
				department.setDepartmentTag(pDept.getDepartmentTag());

				List<Book> books = getBooksFromPDepartment(pDept);
				department.setBooks(books);

				List<Student> students = getStudentsFromPDepartment(pDept);
				department.setStudents(students);
			}
		} catch (InvalidProtocolBufferException e) {
			LOGGER.error("Error occurred while deserializing the stream", e);
			throw new RuntimeException("Error occurred while deserializing the stream", e);
		}

		return department;
	}
	
	private List<Book> getBooksFromPDepartment(final PDepartment department) {
		if (department != null) {
			List<Book> books = new ArrayList<>();
			department.getBooksList().stream()
				.forEach(pBook -> {
					Book book = new Book();
					book.setBookId(pBook.getBookId());
					book.setAuthorId(pBook.getAuthorId());
					book.setAuthorDesc(pBook.getAuthorDesc());
					book.setGenre(Genre.valueOf(pBook.getGenre().name()));

					books.add(book);
				});
			return books;
		}

		return null;
	}
	
	private List<Student> getStudentsFromPDepartment(final PDepartment department) {
		if (department != null) {
			List<Student> students = new ArrayList<>();
			department.getStudentsList().stream()
				.forEach(pStudent -> {
					Student student = new Student();
					student.setStudentId(pStudent.getStudentId().getValue());
					student.setStudentName(pStudent.getStudentName());
					student.setAddress(pStudent.getAddress());
					student.setFavSubject(Subject.valueOf(pStudent.getFavSubject().name()));

					students.add(student);
				});

			return students;
		}

		return null;
	}
	
	private void processBooksBuilder(final List<Book> books, final PDepartment.Builder builder) {
		if (books != null && books.size() > 0) {
			books.stream().forEach(book -> {
				PBook.Builder bookBuilder = PBook.newBuilder();
				bookBuilder.setBookId(book.getBookId());
				bookBuilder.setAuthorId(book.getAuthorId());
				bookBuilder.setAuthorDesc(book.getAuthorDesc());
				bookBuilder.setGenre(PGenre.valueOf(book.getGenre().name()));

				builder.addBooks(bookBuilder);
			});
		}
	}
	
	private void processStudentsBuilder(final List<Student> students, final PDepartment.Builder builder) {
		if (students != null && students.size() > 0) {
			students.stream().forEach(student -> {
				PStudent.Builder studentBuilder = PStudent.newBuilder();
				Builder studentId = getProtoInteger(student.getStudentId());
				studentBuilder.setStudentId(studentId);
				studentBuilder.setStudentName(student.getStudentName());
				studentBuilder.setAddress(student.getAddress());
				studentBuilder.setFavSubject(PSubject.valueOf(student.getFavSubject().name()));

				builder.addStudents(studentBuilder);
			});
		}
	}
	
	private Builder getProtoInteger(final int i) {
		Builder builder = Integer.newBuilder();
		builder.setValue(i);

		return builder;
	}
}
