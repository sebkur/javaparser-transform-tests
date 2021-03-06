package de.topobyte.javatransform;

import java.io.IOException;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

public class MethodReplacer extends BaseModifier
{

	private String methodName;
	private String replacementText;

	public MethodReplacer(CompilationUnit cu, String methodName,
			String replacementText)
	{
		super(cu);
		this.methodName = methodName;
		this.replacementText = replacementText;
	}

	@Override
	public boolean determineWillNeedModifications()
	{
		final boolean[] hasRelevantMethods = { false };

		cu.findAll(ClassOrInterfaceDeclaration.class).stream()
				.filter(c -> !c.isInterface()).forEach(c -> {
					List<MethodDeclaration> methods = c
							.getMethodsByName(methodName);
					hasRelevantMethods[0] |= !methods.isEmpty();
				});

		return hasRelevantMethods[0];
	}

	@Override
	public void transform() throws IOException
	{
		cu.findAll(ClassOrInterfaceDeclaration.class).stream()
				.filter(c -> !c.isInterface()).forEach(c -> {
					modified |= transform(c);
				});
	}

	private boolean transform(ClassOrInterfaceDeclaration c)
	{
		replaceMethods(c, methodName);

		if (modified) {
			System.out.println(c.getName());
		}

		return modified;
	}

	private void replaceMethods(ClassOrInterfaceDeclaration c, String name)
	{
		List<MethodDeclaration> methods = c.getMethodsByName(name);
		for (MethodDeclaration method : methods) {
			method.replace(new FieldDeclaration(
					new NodeList<com.github.javaparser.ast.Modifier>(),
					new ClassOrInterfaceType("REPLACE"), "ME!"));
			modified = true;
		}
	}

	@Override
	public String postTransform(String text)
	{
		return text.replaceAll("REPLACE ME!;", replacementText);
	}

}