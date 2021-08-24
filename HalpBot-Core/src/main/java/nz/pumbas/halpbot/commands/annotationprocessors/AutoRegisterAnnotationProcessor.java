/*
 * MIT License
 *
 * Copyright (c) 2021 pumbas600
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package nz.pumbas.halpbot.commands.annotationprocessors;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes("nz.pumbas.halpbot.annotations.AutoRegister")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class AutoRegisterAnnotationProcessor extends AbstractProcessor
{
    public static final List<String> AnnotatedFullClassNames = new ArrayList<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);

            AnnotatedFullClassNames.addAll(
                annotatedElements.stream()
                .filter(element -> !element.getModifiers().contains(Modifier.ABSTRACT) && !element.getKind().isInterface())
                .map(element -> (PackageElement) element.getEnclosingElement())
                .map(packageElement -> packageElement.getQualifiedName().toString())
                .collect(Collectors.toList())
            );
       }
        try {
            this.generateAutoRegisterUtilFile();
        } catch (IOException e) {
            super.processingEnv.getMessager()
                .printMessage(Kind.ERROR, "There was an error creating the AutoRegisterUtils file");
        }

       return true;
    }

    private void generateAutoRegisterUtilFile() throws IOException {
        JavaFileObject utilFile = super.processingEnv.getFiler()
            .createSourceFile("nz.pumbas.halpbot.annotationprocessors.AutoRegisterUtils");

        try (PrintWriter out = new PrintWriter(utilFile.openWriter())) {
            out.println("package nz.pumbas.halpbot.annotationprocessors;");
            out.println();
            out.println("public class AutoRegisterUtils {");
            out.println();
            out.println("    public List<String> getAnnotatedClasses() {");
            out.println("        return Arrays.asList(");
            out.println("            " + String.join(",\n            ", AnnotatedFullClassNames));
            out.println("        );");
            out.println("    }");
            out.println();
            out.println("}");
        }
    }
}
