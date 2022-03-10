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

package net.pumbas.halpbot.hibernate.repositories;

import org.dockbox.hartshorn.core.domain.Exceptional;

public interface TopicRepository //extends JpaRepository<Topic, Long>
{
    //@Query("SELECT t.id FROM Topic t WHERE t.topic = ?1")
    //List<Long> getIdFromTopic(String topic, Pageable pageable);

    default Exceptional<Long> getFirstIdFromTopic(String topic) {
        //List<Long> id = this.getIdFromTopic(topic, PageRequest.of(0, 1));
        //if (id.isEmpty())
        //    return Exceptional.of(new ResourceNotFoundException("There doesn't appear to be a topic: " + topic));
        //return Exceptional.of(id.get(0));
        return Exceptional.empty();
    }
}